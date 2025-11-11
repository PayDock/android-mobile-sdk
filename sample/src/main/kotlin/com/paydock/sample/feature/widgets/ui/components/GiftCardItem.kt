package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
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
import com.paydock.feature.card.domain.model.integration.GiftCardWidgetConfig
import com.paydock.feature.card.presentation.GiftCardAppearanceDefaults
import com.paydock.feature.card.presentation.GiftCardWidget
import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.style.StylingViewModel

@Composable
fun GiftCardItem(context: Context, stylingViewModel: StylingViewModel) {
    val giftCardAppearance by stylingViewModel.giftCardWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance = giftCardAppearance ?: GiftCardAppearanceDefaults.appearance()
    GiftCardWidget(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        config = GiftCardWidgetConfig(
            accessToken = BuildConfig.ACCESS_TOKEN_WIDGET,
            storePin = true
        ),
        appearance = currentOrDefaultAppearance,
        eventDelegate = object : WidgetEventDelegate {
            override fun widgetEvent(event: Event) {
                Log.d("[GiftCardWidget Event]", "[type=${event.type}] $event")
            }
        },
        completion = { result ->
            result.onSuccess {
                Log.d("[GiftCardWidget]", "Success: $it")
                Toast.makeText(context, "Tokenised card was successful! [$it]", Toast.LENGTH_SHORT)
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
}