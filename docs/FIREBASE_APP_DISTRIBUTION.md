# Automating Android Releases to Firebase App Distribution with GitHub Actions

*A complete, beginner-friendly walkthrough of the CI/CD pipeline set up for **ExpenseTrackr** — a
Kotlin Multiplatform + Compose Multiplatform app. By the end you can build a **debug** or a
**signed release** APK in the cloud and push it to your testers automatically, without ever
committing a secret to a public repository.*

> This document doubles as internal docs and as the source material for a Medium article. It
> explains not just *what* we did but *why* each piece exists.

---

## Table of contents

1. [What we're building](#1-what-were-building)
2. [GitHub Actions in five words](#2-github-actions-in-five-words)
3. [The core problem: secrets on a public repo](#3-the-core-problem-secrets-on-a-public-repo)
4. [Prerequisites](#4-prerequisites)
5. [Firebase side: App Distribution, a tester group, and a service account](#5-firebase-side)
6. [The secrets, and how to generate each one](#6-the-secrets)
7. [Workflow #1 — the debug pipeline (line by line)](#7-workflow-1--the-debug-pipeline)
8. [Workflow #2 — the signed release pipeline](#8-workflow-2--the-signed-release-pipeline)
9. [Typing release notes at run time (workflow inputs)](#9-typing-release-notes-at-run-time)
10. [The "default branch" gotcha](#10-the-default-branch-gotcha)
11. [Running it & reading the logs](#11-running-it--reading-the-logs)
12. [Troubleshooting](#12-troubleshooting)
13. [Security notes](#13-security-notes)
14. [Full reference](#14-full-reference)

---

## 1. What we're building

Two GitHub Actions workflows, both triggered **manually** from the repo's **Actions** tab:

| Workflow file | What it produces | Secrets it needs |
|---|---|---|
| `.github/workflows/firebase-distribution-debug.yml` | An unsigned **debug** APK | 3 (Firebase only) |
| `.github/workflows/firebase-distribution-release.yml` | A **signed release** APK (minified) | 7 (Firebase + keystore) |

Both end by uploading the APK to **Firebase App Distribution**, which emails your tester group a
link to install the new build. We built the debug one first (fewer moving parts), got it green,
then layered signing on top for the release one.

The pipeline in one picture:

```
click "Run workflow"
        │
        ▼
┌─────────────────────────────────────────────────────────────┐
│  Fresh Ubuntu VM (a "runner") — starts completely empty      │
│                                                              │
│  1. checkout   → git clone your repo                         │
│  2. setup-java → install JDK 17                              │
│  3. setup-gradle → install Gradle + caching                  │
│  4. decode google-services.json  ← from a secret             │
│ (5. decode keystore.jks          ← release only)             │
│ (6. recreate keystore.properties ← release only)             │
│  7. ./gradlew assembleDebug|assembleRelease                  │
│  8. upload APK → Firebase App Distribution                   │
└─────────────────────────────────────────────────────────────┘
        │
        ▼
   testers get an email → install the build
```

---

## 2. GitHub Actions in five words

**Run my code in the cloud.** When something happens in your repo (or you click a button),
GitHub spins up a throwaway virtual machine and runs the steps you defined. The vocabulary:

| Term | Meaning |
|---|---|
| **Workflow** | One `.yml` file under `.github/workflows/`. A whole automated process. |
| **Event / trigger** (`on:`) | *What* starts it — a push, a PR, a schedule, or a manual button. |
| **Job** | A group of steps that run together on one machine. |
| **Runner** | The throwaway VM (we use `ubuntu-latest`). It starts **empty**. |
| **Step** | One unit of work — either a shell command (`run:`) or a prebuilt action (`uses:`). |
| **Action** | A reusable package of steps, referenced as `owner/name@version` (like a library). |
| **Secret** | An encrypted value stored in repo settings, injected into a run via `${{ secrets.NAME }}`. |

**The single most important idea:** the runner is a *blank machine*. It knows nothing about your
laptop. Anything it needs — the JDK, Gradle, your Firebase config, your signing key — must be
installed or injected during the run.

---

## 3. The core problem: secrets on a public repo

ExpenseTrackr is a **public** repository. Three files the build needs are deliberately
**gitignored** so they never end up in public git history:

- `androidApp/google-services.json` — Firebase config.
- The release keystore (`*.jks`) — your app-signing key.
- `androidApp/keystore.properties` — the keystore's passwords in plaintext.

Because they're gitignored, `git clone` on the runner won't bring them. So how do you give a blank,
public machine three private files without leaking them?

**Answer: GitHub Secrets.** You store the values (encrypted) in
**Settings → Secrets and variables → Actions**. They are:

- decrypted only inside a running job,
- **auto-masked** in logs (printing one shows `***`),
- **never** exposed to pull requests from forks.

Files (binary or multi-line) are stored **base64-encoded** so they survive as a single text value,
then decoded back into a real file during the run. This base64 round-trip is the workhorse trick of
the whole setup — we use it for `google-services.json` **and** for the keystore.

---

## 4. Prerequisites

- An Android app that already builds locally (ExpenseTrackr targets `com.revanthdev.expensetrackr`).
- Firebase already added to the app (google-services plugin). If your app uploads to Firebase for
  Crashlytics/Analytics, this is already done.
- A release keystore you use to sign the app (only needed for the release workflow).
- Owner/admin access to the GitHub repo (to add secrets).
- Optional: the [GitHub CLI](https://cli.github.com/) (`gh`) for adding secrets from the terminal.

Build tooling versions this pipeline was written against: **AGP 9.0.1**, **Gradle 9.1**, **JDK 17**.

---

## 5. Firebase side

### 5a. Enable App Distribution + create a tester group

1. [Firebase console](https://console.firebase.google.com) → your project.
2. **Release & Monitor → App Distribution → Get started.**
3. Open the **Testers & Groups** tab → **Add group**. Give it a name; note its **alias** (the
   machine-readable id — sometimes differs from the display name). In this project the alias is
   **`revanthTesters`**, which is the value in the workflows' `groups:` field.
4. Add at least your own email as a tester in that group.

### 5b. Find your Firebase App ID

**Project settings (⚙️) → General → Your apps → App ID.** It looks like
`1:1234567890:android:abc123def456`. This is *not* your `applicationId`; it's a Firebase-specific id.

### 5c. Create a service account (the upload permission)

The upload needs to authenticate to Google as something that's allowed to publish builds. We use a
**service account** — a non-human Google identity described by a JSON key file.

1. **Project settings → Service accounts → Generate new private key → Generate key.**
2. A `.json` file downloads. **Treat it like a password** — it grants access to your Firebase
   project. Never commit it.
3. If the upload later fails with a *permissions* error, grant the account the **Firebase App
   Distribution Admin** role in Google Cloud Console → *IAM & Admin → IAM*.

---

## 6. The secrets

Add these under **GitHub repo → Settings → Secrets and variables → Actions → New repository
secret**. Names are **case-sensitive** and must match the workflows exactly.

| Secret name | Used by | What it holds | How to produce the value |
|---|---|---|---|
| `FIREBASE_APP_ID` | both | The `1:…:android:…` id | Copy from Firebase → Project settings → General |
| `FIREBASE_SERVICE_ACCOUNT` | both | The **raw** service-account JSON | `pbcopy < ~/Downloads/service-account.json` then paste |
| `GOOGLE_SERVICES_JSON` | both | **base64** of `google-services.json` | `base64 -i androidApp/google-services.json \| pbcopy` |
| `KEYSTORE_BASE64` | release | **base64** of your `.jks` keystore | `base64 -i /path/to/your-release.jks \| pbcopy` |
| `KEYSTORE_PASSWORD` | release | Keystore (store) password | The `storePassword` from your `keystore.properties` |
| `KEY_ALIAS` | release | The key alias | The `keyAlias` from your `keystore.properties` |
| `KEY_PASSWORD` | release | The key password | The `keyPassword` from your `keystore.properties` |

> **base64 vs raw:** `FIREBASE_SERVICE_ACCOUNT` goes in **raw** because the upload action expects
> JSON text directly. `GOOGLE_SERVICES_JSON` and `KEYSTORE_BASE64` go in **base64** because our
> workflow decodes them back into files. (On Linux — the runner — `base64 --decode` ignores the
> line wraps macOS adds, so wrapping is harmless.)

**Adding secrets from the terminal (optional):**

```bash
gh secret set FIREBASE_APP_ID --body "1:1234567890:android:abc123"
gh secret set FIREBASE_SERVICE_ACCOUNT < ~/Downloads/service-account.json
base64 -i androidApp/google-services.json | gh secret set GOOGLE_SERVICES_JSON
base64 -i /path/to/your-release.jks      | gh secret set KEYSTORE_BASE64
gh secret set KEYSTORE_PASSWORD --body "…"
gh secret set KEY_ALIAS         --body "…"
gh secret set KEY_PASSWORD      --body "…"
```

Piping from a file (rather than pasting) keeps the value out of your clipboard and shell history.

---

## 7. Workflow #1 — the debug pipeline

File: `.github/workflows/firebase-distribution-debug.yml`

```yaml
name: Firebase Distribution — Debug   # Display name in the GitHub "Actions" tab.

# `on:` answers "WHEN does this run?". `workflow_dispatch` = only when a human
# clicks the "Run workflow" button in the Actions tab. Nothing runs automatically.
on:
  workflow_dispatch:

jobs:
  distribute:                       # `distribute` is an id we invented.
    name: Build & distribute debug APK
    runs-on: ubuntu-latest          # The runner: a blank Ubuntu VM.
    steps:

      # STEP 1 — Clone your repo onto the blank runner.
      - name: Checkout repository
        uses: actions/checkout@v4

      # STEP 2 — Install JDK 17 (AGP 9 / Gradle 9.1 need it to RUN the build,
      # separate from the app's JVM_11 compile target).
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      # STEP 3 — Install Gradle + enable dependency caching (faster later runs).
      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4

      # STEP 4 — Recreate google-services.json from a base64 secret.
      # Firebase's Gradle plugin REQUIRES the file or the build fails.
      - name: Create google-services.json
        env:
          GOOGLE_SERVICES_JSON: ${{ secrets.GOOGLE_SERVICES_JSON }}
        run: echo "$GOOGLE_SERVICES_JSON" | base64 --decode > androidApp/google-services.json

      # STEP 5 — Build the debug APK. Debug uses auto-generated signing → no keystore.
      # Output: androidApp/build/outputs/apk/debug/androidApp-debug.apk
      - name: Build debug APK
        run: ./gradlew :androidApp:assembleDebug --stacktrace

      # STEP 6 — Upload the APK to Firebase App Distribution.
      - name: Upload to Firebase App Distribution
        uses: wzieba/Firebase-Distribution-Github-Action@v1
        with:
          appId: ${{ secrets.FIREBASE_APP_ID }}
          serviceCredentialsFileContent: ${{ secrets.FIREBASE_SERVICE_ACCOUNT }}
          groups: revanthTesters
          file: androidApp/build/outputs/apk/debug/androidApp-debug.apk
          releaseNotes: "Automated debug build from GitHub Actions (${{ github.sha }})"
```

### How to read the YAML

- Indentation is meaningful (spaces, **never tabs**). A `-` starts a list item — every step is one.
- `${{ … }}` is an **expression** evaluated at run time. Inside it you read *contexts*:
  `secrets.X` (your encrypted values), `github.sha` (the commit that triggered the run), etc.
- Two kinds of step: `uses:` runs a **published action**; `run:` runs **your shell commands**.
- **Why `env:` in step 4?** Funneling a secret through an environment variable (`$GOOGLE_SERVICES_JSON`)
  keeps it out of the literal command text and avoids shell-quoting problems — a good habit.
- **Pinning versions** (`@v4`, `@v1`) protects you from surprise breaking changes upstream.

---

## 8. Workflow #2 — the signed release pipeline

File: `.github/workflows/firebase-distribution-release.yml`

It's the debug workflow **plus signing**. The key insight: this project's
`androidApp/build.gradle.kts` already reads its signing config from
`androidApp/keystore.properties`, and only uses the real key **when that file exists**:

```kotlin
val keystorePropsFile = file("keystore.properties")
// …
signingConfigs {
    create("release") {
        if (keystorePropsFile.exists()) {
            storeFile = file(keystoreProps.getProperty("storeFile"))
            storePassword = keystoreProps.getProperty("storePassword")
            keyAlias = keystoreProps.getProperty("keyAlias")
            keyPassword = keystoreProps.getProperty("keyPassword")
        }
    }
}
buildTypes {
    getByName("release") {
        // …minify + shrink…
        signingConfig = if (keystorePropsFile.exists())
            signingConfigs.getByName("release")   // real key on CI / your machine
        else
            signingConfigs.getByName("debug")     // fallback so open-source clones still build
    }
}
```

So on CI we only have to **put two files back**: the `.jks` keystore, and `keystore.properties`
pointing at it. That's steps 5 and 6 below (the rest matches debug).

```yaml
name: Firebase Distribution — Release

on:
  workflow_dispatch:
    inputs:                                   # form fields on the "Run workflow" dialog
      releaseNotes:
        description: "Release notes for testers (shown in Firebase)"
        required: true
        type: string
        default: "Bug fixes and improvements"

jobs:
  distribute:
    name: Build & distribute signed release APK
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Create google-services.json
        env:
          GOOGLE_SERVICES_JSON: ${{ secrets.GOOGLE_SERVICES_JSON }}
        run: echo "$GOOGLE_SERVICES_JSON" | base64 --decode > androidApp/google-services.json

      # STEP 5 — Recreate the release keystore (.jks) from a base64 secret.
      - name: Recreate release keystore
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
        run: echo "$KEYSTORE_BASE64" | base64 --decode > androidApp/release.jks

      # STEP 6 — Recreate androidApp/keystore.properties. `storeFile` is resolved
      # relative to the androidApp module, so `release.jks` points at step 5's file.
      - name: Recreate keystore.properties
        env:
          STORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: |
          printf 'storeFile=release.jks\nstorePassword=%s\nkeyAlias=%s\nkeyPassword=%s\n' \
            "$STORE_PASSWORD" "$KEY_ALIAS" "$KEY_PASSWORD" > androidApp/keystore.properties

      # STEP 7 — Build the SIGNED RELEASE APK (minify + shrink + sign with real key).
      # Output: androidApp/build/outputs/apk/release/androidApp-release.apk
      - name: Build release APK
        run: ./gradlew :androidApp:assembleRelease --stacktrace

      - name: Upload to Firebase App Distribution
        uses: wzieba/Firebase-Distribution-Github-Action@v1
        with:
          appId: ${{ secrets.FIREBASE_APP_ID }}
          serviceCredentialsFileContent: ${{ secrets.FIREBASE_SERVICE_ACCOUNT }}
          groups: revanthTesters
          file: androidApp/build/outputs/apk/release/androidApp-release.apk
          releaseNotes: ${{ inputs.releaseNotes }}
```

### The two signing steps explained

- **Step 5 — decode the keystore.** A `.jks` is a *binary* file, so it's stored base64 in
  `KEYSTORE_BASE64` and decoded back to `androidApp/release.jks`. Same trick as `google-services.json`.
- **Step 6 — recreate `keystore.properties`.** `printf` writes the four `key=value` lines Gradle
  expects. `storeFile=release.jks` is *relative* to the `androidApp` module, so it resolves to the
  file step 5 just wrote. The three sensitive values come from secrets and are auto-masked in logs.
- **Step 7 — `assembleRelease`.** Because `keystore.properties` now exists, Gradle signs with your
  real key. This variant also minifies and shrinks resources (via R8/ProGuard), so it's slower than
  debug.

---

## 9. Typing release notes at run time

Because the release trigger is `workflow_dispatch`, we can attach **inputs** — form fields shown in
the "Run workflow" dialog:

```yaml
on:
  workflow_dispatch:
    inputs:
      releaseNotes:
        description: "Release notes for testers (shown in Firebase)"
        required: true
        type: string
        default: "Bug fixes and improvements"
```

Then we read the typed value with `${{ inputs.releaseNotes }}` and pass it to the upload step's
`releaseNotes:`. So every release you kick off, you type fresh notes; testers see exactly that text
in Firebase. Input `type:` can also be `boolean`, `choice`, or `environment` — useful later for,
say, a dropdown of tester groups.

---

## 10. The "default branch" gotcha

For `workflow_dispatch`, **the "Run workflow" button only appears once the workflow file exists on
your repository's default branch** (usually `main`). If you commit the workflow on a feature branch,
you won't see the button until it's merged to `main`. Likewise, changes to the input fields only
show up in the dialog after they reach the branch you run from. This one confuses almost everyone the
first time.

---

## 11. Running it & reading the logs

1. Push the workflow(s) to your **default branch**.
2. GitHub repo → **Actions** tab → pick **Firebase Distribution — Debug** (or Release) in the left
   list → **Run workflow** → (Release: type your notes) → **Run workflow**.
3. Click the run to watch it live. Each step is expandable; a green ✔ means it passed, a red ✖ shows
   exactly where it failed with logs.
4. On success, your tester group gets an email from Firebase with an install link. You can also see
   the build under **App Distribution → Releases** in the Firebase console.

---

## 12. Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| No **Run workflow** button | Workflow isn't on the **default branch** yet (see §10). |
| `File google-services.json is missing` | `GOOGLE_SERVICES_JSON` secret missing/mis-named, or not base64. |
| Release build fails at signing / `keystore was tampered with, or password was incorrect` | Wrong `KEYSTORE_PASSWORD`/`KEY_PASSWORD`, or a corrupted `KEYSTORE_BASE64` (re-encode the `.jks`). |
| `Group not found` from the upload step | `groups:` value must be the group **alias**, not its display name. |
| Upload fails with a **permission** error | Grant the service account **Firebase App Distribution Admin** in Google Cloud IAM (see §5c). |
| APK not found at the `file:` path | Module or variant path changed — confirm the `build/outputs/apk/...` path matches your module name. |

Tip: add `--stacktrace` (already present) to Gradle for fuller error output, or temporarily bump to
`--info` while debugging a build issue.

---

## 13. Security notes

- **Never commit** `google-services.json`, the `.jks`, or `keystore.properties`. They're gitignored
  for a reason; secrets replace them on CI.
- Your **keystore password cannot be recovered from the Play Store** — Google never shows it. Keep a
  backup of the `.jks` **and** its passwords somewhere safe (a password manager). If you lose the
  upload key and use **Play App Signing**, you can request an upload-key reset in the Play Console;
  the app signing key itself stays safe with Google.
- Secrets are masked in logs, but **anyone who can push workflow changes can exfiltrate them** — so
  protect your default branch and review workflow edits from contributors.
- Prefer a **service account** over long-lived personal `firebase login:ci` tokens; scope it to
  App Distribution only.

---

## 14. Full reference

**Files added**

```
.github/workflows/firebase-distribution-debug.yml     # manual → debug APK → Firebase
.github/workflows/firebase-distribution-release.yml   # manual → signed release APK → Firebase
```

**Secrets used**

| Secret | Debug | Release |
|---|:---:|:---:|
| `FIREBASE_APP_ID` | ✅ | ✅ |
| `FIREBASE_SERVICE_ACCOUNT` | ✅ | ✅ |
| `GOOGLE_SERVICES_JSON` | ✅ | ✅ |
| `KEYSTORE_BASE64` | — | ✅ |
| `KEYSTORE_PASSWORD` | — | ✅ |
| `KEY_ALIAS` | — | ✅ |
| `KEY_PASSWORD` | — | ✅ |

**Key facts for this project**

- App: `com.revanthdev.expensetrackr` · Firebase tester group alias: `revanthTesters`
- Tooling: AGP 9.0.1 · Gradle 9.1 · JDK 17 (Temurin)
- Actions used: `actions/checkout@v4`, `actions/setup-java@v4`, `gradle/actions/setup-gradle@v4`,
  `wzieba/Firebase-Distribution-Github-Action@v1`

**Ideas to extend later**

- Add a `push:` trigger (e.g. to `main`) for fully automatic distribution.
- Trigger on git **tags** (`v*`) to distribute only on tagged releases.
- Build an **AAB** and upload to the Play Store's internal testing track instead.
- Auto-bump `versionCode` from the run number (`${{ github.run_number }}`).
- Cache more aggressively, or split build & distribute into separate jobs with an artifact upload.
```
