package com.paydock.feature.zip.presentation

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import com.paydock.core.MobileSDKConstants
import com.paydock.feature.zip.domain.model.ZipCallbackData
import com.paydock.feature.zip.domain.model.ZipStatus
import com.paydock.feature.zip.presentation.components.ZipWebView
import com.paydock.feature.zip.presentation.utils.getCheckoutUrlExtra
import com.paydock.feature.zip.presentation.utils.putErrorCodeExtra
import com.paydock.feature.zip.presentation.utils.putErrorMessageExtra
import com.paydock.feature.zip.presentation.utils.putResultCheckoutIdExtra
import com.paydock.feature.zip.presentation.utils.putResultOrderIdExtra
import com.paydock.feature.zip.presentation.utils.putResultStatusExtra

/**
 * An activity that handles the Zip web payment flow.
 *
 * This activity launches a WebView to display the Zip checkout page and handles the
 * interaction between the user and the payment provider. It manages the lifecycle of the
 * payment process, including handling success, failure, and cancellation scenarios.
 *
 * The Zip WebView content includes its own navigation UI (e.g. close button on hosted pages).
 */
internal class ZipWebActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val checkoutUrl = intent.getCheckoutUrlExtra()
        if (!isValidWebViewUrl(checkoutUrl)) {
            finishWithError(null, MobileSDKConstants.ZipConfig.Errors.INVALID_URL)
            return
        }
        val url = checkoutUrl!!

        // Handle back press as user cancellation
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishWithCancellation()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setContent {
            enableEdgeToEdge()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                ZipWebView(
                    checkoutUrl = url,
                    onApprove = { callbackData ->
                        finishWithSuccess(callbackData)
                    },
                    onFailure = { callbackData ->
                        finishWithFailure(callbackData)
                    },
                    onWebViewError = { errorCode, errorMessage ->
                        finishWithError(errorCode, errorMessage)
                    }
                )
            }
        }

        // Sets the window layout to match the parent dimensions.
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun finishWithSuccess(callbackData: ZipCallbackData) {
        setResult(
            RESULT_OK,
            Intent()
                .putResultStatusExtra(callbackData.status.value)
                .putResultCheckoutIdExtra(callbackData.checkoutId)
                .putResultOrderIdExtra(callbackData.orderId)
        )
        finish()
    }

    private fun finishWithFailure(callbackData: ZipCallbackData) {
        setResult(
            RESULT_CANCELED,
            Intent()
                .putResultStatusExtra(callbackData.status.value)
                .putResultCheckoutIdExtra(callbackData.checkoutId)
                .putResultOrderIdExtra(callbackData.orderId)
        )
        finish()
    }

    private fun finishWithError(errorCode: Int?, errorMessage: String) {
        setResult(
            RESULT_CANCELED,
            Intent()
                .putResultStatusExtra(ZipStatus.UNEXPECTED.value)
                .putErrorCodeExtra(errorCode)
                .putErrorMessageExtra(errorMessage)
        )
        finish()
    }

    private fun finishWithCancellation() {
        setResult(
            RESULT_CANCELED,
            Intent()
                .putResultStatusExtra(ZipStatus.CANCELLED.value)
        )
        finish()
    }

    private fun isValidWebViewUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val trimmed = url.trim()
        return trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
    }
}
