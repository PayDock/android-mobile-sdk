package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.core.domain.model.Event
import com.paydock.core.presentation.util.WidgetEventDelegate
import com.paydock.feature.card.presentation.GiftCardAppearanceDefaults
import com.paydock.feature.card.presentation.GiftCardWidget
import com.paydock.feature.card.presentation.state.rememberGiftCardWidgetState
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.style.StylingViewModel

@Composable
fun GiftCardItem(
    context: Context,
    stylingViewModel: StylingViewModel,
    configViewModel: ConfigViewModel
) {
    val giftCardAppearance by stylingViewModel.giftCardWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance = giftCardAppearance ?: GiftCardAppearanceDefaults.appearance()
    val giftCardConfig by configViewModel.giftCardWidgetConfig.collectAsState()
    // Demo state handle for driving submission from a custom button when
    // `giftCardConfig.showSubmitButton` is turned off in the Config screen.
    val giftCardWidgetState = rememberGiftCardWidgetState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GiftCardWidget(
            config = giftCardConfig,
            appearance = currentOrDefaultAppearance,
            eventDelegate = object : WidgetEventDelegate {
                override fun widgetEvent(event: Event) {
                    Log.d("[GiftCardWidget Event]", "[type=${event.type}] $event")
                }
            },
            state = giftCardWidgetState,
            completion = { result ->
                result.onSuccess {
                    Log.d("[GiftCardWidget]", "Success: $it")
                    Toast.makeText(
                        context,
                        "Tokenised card was successful! [$it]",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }.onFailure {
                    val error = it.toError()
                    Log.d("[GiftCardWidget]", "Failure: ${error.displayableMessage}")
                    Toast.makeText(
                        context,
                        "Tokenised card failed! [${error.displayableMessage}]",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        // Demo custom submit button — shown only when the widget's own button is hidden via
        // `showSubmitButton = false` in the Config screen.
        if (!giftCardConfig.showSubmitButton) {
            // Mirrors the internal button's own enabled logic: when `activePrimaryButton` is true the
            // button stays enabled and validates on tap; otherwise it stays disabled until valid.
            AppButton(
                text = "Submit (custom button)",
                modifier = Modifier.fillMaxWidth(),
                enabled = giftCardConfig.activePrimaryButton || giftCardWidgetState.isFormValid,
                onClick = { giftCardWidgetState.submit() }
            )
        }
    }
}