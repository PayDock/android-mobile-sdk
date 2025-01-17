package com.paydock.sample.feature.checkout

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.feature.src.domain.model.integration.meta.ClickToPayMeta
import com.paydock.feature.src.presentation.ClickToPayWidget
import com.paydock.sample.BuildConfig

class ClickToPayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
        // Adds the back button callback to the dispatcher.
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setContent {
            val accessToken = intent.getStringExtra("accessToken") ?: ""
            ClickToPayScreen(accessToken) { result ->
                val intent = Intent().putExtra("isSuccess", result.isSuccess)
                result.onSuccess {
                    intent.putExtra("token", it)
                    setResult(RESULT_OK, intent)
                    finish()
                }.onFailure {
                    val error = it.toError()
                    intent.putExtra("message", error.displayableMessage)
                    setResult(RESULT_OK, intent)
                }
            }
        }
    }
}

@Composable
fun ClickToPayScreen(accessToken: String, resultHandler: (Result<String>) -> Unit) {
    ClickToPayWidget(
        modifier = Modifier.fillMaxWidth(),
        accessToken = accessToken,
        serviceId = BuildConfig.GATEWAY_ID_MASTERCARD_SRC,
        meta = ClickToPayMeta(disableSummaryScreen = true),
        completion = resultHandler
    )
}