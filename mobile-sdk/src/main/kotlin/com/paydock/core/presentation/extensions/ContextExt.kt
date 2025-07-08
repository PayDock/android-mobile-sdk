package com.paydock.core.presentation.extensions

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

/**
 * Extension function for checking if a particular permission is granted.
 *
 * @param permission The permission to be checked.
 * @return `true` if the permission is granted, otherwise `false`.
 */
internal fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) ==
        PackageManager.PERMISSION_GRANTED
}

/**
 * Extension function to open a share sheet with the specified title, message, and optional intent title.
 *
 * @param title The title of the shared content.
 * @param message The message or content to be shared.
 * @param intentTitle Optional title for the sharing intent chooser dialog.
 */
internal fun Context.openShareSheet(
    title: String,
    message: String,
    intentTitle: String = title,
) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }
    startActivity(Intent.createChooser(shareIntent, intentTitle))
}

/**
 * Extension function to display a toast with the specified message and duration.
 *
 * @param message The message to be displayed in the toast.
 * @param duration The duration of the toast display (default is [Toast.LENGTH_SHORT]).
 */
internal fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Extension function to safely open a browser with the specified URL string.
 * It checks if there's an app available to handle the intent.
 *
 * @param urlString The URL string to be opened in the browser.
 * @param onError A lambda to be invoked if the URL is invalid or no browser application is found.
 *                Defaults to showing a Toast message.
 */
internal fun Context.openBrowser(
    urlString: String,
    onError: (failedUrl: String, errorType: UrlErrorType) -> Unit = { failedUrl, errorType ->
        val message = when (errorType) {
            UrlErrorType.INVALID_URL -> "Invalid URL format: $failedUrl"
            UrlErrorType.NO_HANDLER -> "No application found to open URL: $failedUrl"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
) {
    try {
        // Use androidx.core.net.toUri() for better null safety and exception handling
        // if you are not already. Otherwise, Uri.parse() can throw IllegalArgumentException.
        val uri = urlString.toUri()
        openBrowser(uri) { failedUri -> onError(failedUri.toString(), UrlErrorType.NO_HANDLER) }
    } catch (_: IllegalArgumentException) {
        // Handle cases where the URL string itself is malformed
        onError(urlString, UrlErrorType.INVALID_URL)
    }
}

/**
 * Extension function to safely open a browser with the specified URI.
 * It checks if there's an app available to handle the intent.
 *
 * @param uri The URI to be opened in the browser.
 * @param onError A lambda to be invoked if no browser application is found to handle the URI.
 *                Defaults to showing a Toast message.
 */
internal fun Context.openBrowser(
    uri: Uri,
    onError: (failedUri: Uri) -> Unit = { failedUri ->
        Toast.makeText(
            this,
            "No application found to open URI: $failedUri", // Consider using a string resource
            Toast.LENGTH_LONG
        ).show()
    }
) {
    try {
        val browserIntent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
        }
        if (browserIntent.resolveActivity(this.packageManager) != null) {
            startActivity(browserIntent)
        } else {
            onError(uri)
        }
    } catch (_: Exception) {
        onError(uri)
    }
}

enum class UrlErrorType {
    INVALID_URL,
    NO_HANDLER
}

/**
 * Extension function to retrieve the [AppCompatActivity] from a [Context].
 *
 * This function is useful when you need to extract the current [AppCompatActivity]
 * from any given [Context]. It recursively checks if the context is an instance of
 * [AppCompatActivity] or a [ContextWrapper] containing the activity. If the context
 * doesn't represent an activity or contain one, it returns `null`.
 *
 * @return The [AppCompatActivity] if found, otherwise `null`.
 */
internal fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

internal inline fun <reified Activity : ComponentActivity> Context.getActivity(): Activity? {
    return when (this) {
        is Activity -> this
        else -> {
            var context = this
            while (context is ContextWrapper) {
                context = context.baseContext
                if (context is Activity) return context
            }
            null
        }
    }
}

/**
 * Reads the text content from an asset file.
 *
 * @param fileName The name of the asset file.
 * @return The text content of the asset file.
 */
fun Context.readTextFromAsset(fileName: String): String {
    return assets.open(fileName).bufferedReader().use {
        it.readText()
    }
}

/**
 * Reads the text content from a resource file.
 *
 * @param fileName The name of the resource file.
 * @return The text content of the resource file.
 */
fun Context.readTextFromResources(fileName: String): String {
    return resources.openRawResource(
        resources.getIdentifier(fileName, "raw", packageName)
    ).bufferedReader().use { it.readText() }
}