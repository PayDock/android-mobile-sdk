/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 4:15 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.core.presentation.ui.extensions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

/**
 * Extension function for checking if a particular permission is granted.
 *
 * @param permission The permission to be checked.
 * @return `true` if the permission is granted, otherwise `false`.
 */
fun Context.checkPermission(permission: String): Boolean {
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
fun Context.openShareSheet(
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
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Extension function to open a browser with the specified URL.
 *
 * @param url The URL to be opened in the browser.
 */
fun Context.openBrowser(url: String) {
    startActivity(
        Intent(Intent.ACTION_VIEW).apply {
            data = url.toUri()
        }
    )
}
