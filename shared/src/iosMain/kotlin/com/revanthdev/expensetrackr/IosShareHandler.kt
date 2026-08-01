package com.revanthdev.expensetrackr

import com.revanthdev.expensetrackr.core.presentation.ShareHandler
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

/** iOS share sheet via UIActivityViewController, presented from the key window's root controller. */
class IosShareHandler : ShareHandler {
    override fun share(text: String): Boolean = runCatching {
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return false
        val controller = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )
        root.presentViewController(controller, animated = true, completion = null)
        true
    }.getOrDefault(false)
}
