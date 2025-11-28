package com.paydock.sample.feature.checkout

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.feature.src.domain.model.integration.ClickToPayWidgetConfig
import com.paydock.feature.src.domain.model.integration.meta.ClickToPayMeta
import com.paydock.feature.src.presentation.ClickToPayAppearanceDefaults
import com.paydock.feature.src.presentation.ClickToPayWidget
import com.paydock.sample.BuildConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.CenterAppTopBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
            ClickToPayScreen { result ->
                val intent = Intent().putExtra("isSuccess", result.isSuccess)
                result.onSuccess {
                    setResult(
                        RESULT_OK,
                        intent.putExtra("token", it)
                    )
                    finish()
                }.onFailure {
                    val error = it.toError()
                    setResult(
                        RESULT_OK,
                        intent.putExtra("message", error.displayableMessage)
                    )
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickToPayScreen(resultHandler: (Result<String>) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAppTopBar(
                title = stringResource(R.string.label_click_to_pay),
                showTitle = true,
                onBackButtonClick = {
                    resultHandler(Result.failure(Exception("User cancelled")))
                }
            )
        }
    ) { paddingValues ->
        ClickToPayWidget(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            config = ClickToPayWidgetConfig(
                accessToken = BuildConfig.ACCESS_TOKEN_WIDGET,
                serviceId = BuildConfig.SERVICE_ID_CLICK_TO_PAY,
                meta = ClickToPayMeta(disableSummaryScreen = true),
            ),
            appearance = ClickToPayAppearanceDefaults.appearance(),
            completion = resultHandler
        )
    }
}