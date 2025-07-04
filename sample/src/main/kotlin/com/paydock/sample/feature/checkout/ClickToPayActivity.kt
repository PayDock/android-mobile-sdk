package com.paydock.sample.feature.checkout

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.feature.src.domain.model.integration.ClickToPayWidgetConfig
import com.paydock.feature.src.domain.model.integration.meta.ClickToPayMeta
import com.paydock.feature.src.presentation.ClickToPayAppearanceDefaults
import com.paydock.feature.src.presentation.ClickToPayWidget
import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.style.StylingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClickToPayActivity : ComponentActivity() {
    private val stylingViewModel: StylingViewModel by viewModels()

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
            ClickToPayScreen(stylingViewModel) { result ->
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
fun ClickToPayScreen(stylingViewModel: StylingViewModel, resultHandler: (Result<String>) -> Unit) {
    val clickToPayAppearance by stylingViewModel.clickToPayWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance =
        clickToPayAppearance ?: ClickToPayAppearanceDefaults.appearance()
    ClickToPayWidget(
        modifier = Modifier.fillMaxWidth().safeDrawingPadding(),
        config = ClickToPayWidgetConfig(
            accessToken = BuildConfig.WIDGET_ACCESS_TOKEN,
            serviceId = BuildConfig.GATEWAY_ID_CLICK_TO_PAY,
            meta = ClickToPayMeta(disableSummaryScreen = true),
        ),
        appearance = currentOrDefaultAppearance,
        completion = resultHandler
    )
}