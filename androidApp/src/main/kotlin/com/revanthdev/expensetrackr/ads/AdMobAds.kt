package com.revanthdev.expensetrackr.ads

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.revanthdev.expensetrackr.BuildConfig

/** Anchored adaptive banner. Its ID is generated from the gitignored local.properties. */
@Composable
fun AdMobBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val width = maxWidth.value.toInt().coerceAtLeast(1)
        val adView = remember(width) {
            AdView(context).apply {
                adUnitId = BuildConfig.ADMOB_BANNER_ID
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width))
                loadAd(AdRequest.Builder().build())
            }
        }

        DisposableEffect(adView) {
            onDispose { adView.destroy() }
        }

        AndroidView(
            factory = { adView },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Loads one Google test native ad and releases it when the list slot leaves composition. */
@Composable
fun AdMobNative(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(context) {
        var active = true
        val loader = AdLoader.Builder(context, BuildConfig.ADMOB_NATIVE_ID)
            .forNativeAd { loadedAd ->
                if (active) {
                    nativeAd?.destroy()
                    nativeAd = loadedAd
                } else {
                    loadedAd.destroy()
                }
            }
            .build()
        loader.loadAd(AdRequest.Builder().build())

        onDispose {
            active = false
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    nativeAd?.let { ad ->
        AndroidView(
            factory = { createNativeAdView(it, ad) },
            update = { it.setNativeAd(ad) },
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
        )
    }
}

private fun createNativeAdView(context: android.content.Context, ad: NativeAd): NativeAdView {
    fun Int.dp(): Int = (this * context.resources.displayMetrics.density).toInt()
    fun textView(size: Float, color: Int = Color.DKGRAY) = TextView(context).apply {
        textSize = size
        setTextColor(color)
        maxLines = 2
    }

    val root = NativeAdView(context).apply {
        setPadding(12.dp(), 10.dp(), 12.dp(), 10.dp())
        background = GradientDrawable().apply {
            setColor(Color.rgb(248, 249, 250))
            cornerRadius = 14.dp().toFloat()
            setStroke(1.dp(), Color.rgb(218, 220, 224))
        }
    }
    val column = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        )
    }
    val attribution = textView(11f, Color.rgb(95, 99, 104)).apply { text = "Ad" }
    column.addView(attribution)

    val header = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, 6.dp(), 0, 6.dp())
    }
    val icon = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        layoutParams = LinearLayout.LayoutParams(48.dp(), 48.dp()).apply {
            marginEnd = 10.dp()
        }
    }
    val titles = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }
    val headline = textView(16f, Color.rgb(32, 33, 36)).apply {
        setTypeface(typeface, Typeface.BOLD)
        text = ad.headline
    }
    val advertiser = textView(12f).apply {
        text = ad.advertiser.orEmpty()
        visibility = if (ad.advertiser.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    titles.addView(headline)
    titles.addView(advertiser)
    header.addView(icon)
    header.addView(titles)
    column.addView(header)

    val media = MediaView(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            140.dp(),
        )
    }
    column.addView(media)

    val body = textView(13f).apply {
        text = ad.body.orEmpty()
        visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE
        setPadding(0, 8.dp(), 0, 8.dp())
    }
    column.addView(body)

    val cta = Button(context).apply {
        text = ad.callToAction.orEmpty()
        visibility = if (ad.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
        isAllCaps = false
    }
    column.addView(cta)
    root.addView(column)

    root.headlineView = headline
    root.advertiserView = advertiser
    root.iconView = icon
    root.mediaView = media
    root.bodyView = body
    root.callToActionView = cta
    icon.setImageDrawable(ad.icon?.drawable)
    icon.visibility = if (ad.icon == null) View.GONE else View.VISIBLE
    root.setNativeAd(ad)
    return root
}
