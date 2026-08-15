package com.l1khith.calender28.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object UrlLauncher {

    /**
     * Safely launches an external web browser to open the specified URL.
     * Displays a platform toast if no web browser is available or if the link is invalid.
     */
    fun openBrowser(context: Context, url: String) {
        try {
            val parsedUri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            PlatformUtils.showToast(context, "No web browser found to open link")
        } catch (e: Exception) {
            PlatformUtils.showToast(context, "Unable to open link: ${e.localizedMessage}")
        }
    }
}
