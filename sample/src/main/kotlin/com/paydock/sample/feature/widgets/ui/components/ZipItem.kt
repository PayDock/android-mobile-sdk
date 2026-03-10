package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.exceptions.GenericException
import com.paydock.core.domain.error.exceptions.ZipException
import com.paydock.core.domain.error.toError
import com.paydock.core.domain.model.Event
import com.paydock.core.presentation.util.WidgetEventDelegate
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.feature.zip.presentation.ZipWidget
import com.paydock.feature.zip.presentation.ZipWidgetAppearanceDefaults
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.style.StylingViewModel

@Composable
fun ZipItem(
    context: Context,
    stylingViewModel: StylingViewModel,
    configViewModel: ConfigViewModel
) {
    val zipAppearance by stylingViewModel.zipWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance =
        zipAppearance ?: ZipWidgetAppearanceDefaults.appearance()
    val zipConfig by configViewModel.zipWidgetConfig.collectAsState()

    // Add hardcoded items and statistics to the config
    val enhancedZipConfig = zipConfig.copy(
        items = listOf(
            ZipWidgetConfig.Item(
                name = "ACME Toolbox",
                amount = "2",
                quantity = 1,
                reference = "Fuga consequuntur sint ab magnam"
            ),
            ZipWidgetConfig.Item(
                name = "Device 42",
                amount = "2",
                quantity = 1,
                reference = "Fuga consequuntur sint ab magnam"
            )
        ),
        statistics = ZipWidgetConfig.Statistics(
            accountCreated = "2017-05-05",
            salesTotalNumber = "5",
            salesTotalAmount = "4",
            salesAvgValue = "45",
            salesMaxValue = "400",
            refundsTotalAmount = "21",
            previousChargeback = "true",
            currency = zipConfig.currency,
            lastLogin = "2017-06-01"
        )
    )

    ZipWidget(
        modifier = Modifier.padding(16.dp),
        config = enhancedZipConfig,
        eventDelegate = object : WidgetEventDelegate {
            override fun widgetEvent(event: Event) {
                Log.d("[ZipWidget Event]", "[type=${event.type}] $event")
            }
        },
        appearance = currentOrDefaultAppearance
    ) { result ->
        result.onSuccess {
            Log.d("[ZipWidget]", "Success: $it")
            Toast.makeText(context, "Zip Result returned [Token: ${it.token}]", Toast.LENGTH_SHORT)
                .show()
        }.onFailure { exception: Throwable ->
            if (exception is GenericException) {
                val error = exception.toError().displayableMessage
                Log.d("[ZipWidget]", "Failure: $error")
                Toast.makeText(
                    context,
                    "Zip tokenisation failed! [${error}]",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else if (exception is ZipException) {
                val error = when (exception) {
                    is ZipException.CancellationException -> exception.toError().displayableMessage
                    is ZipException.CreatingPaymentSourceTokenException -> exception.toError().displayableMessage
                    is ZipException.DeclinedException -> exception.toError().displayableMessage
                    is ZipException.FetchingCheckoutUrlException -> exception.toError().displayableMessage
                    is ZipException.ParseException -> exception.toError().displayableMessage
                    is ZipException.ReferredException -> exception.toError().displayableMessage
                    is ZipException.UnexpectedStatusException -> exception.toError().displayableMessage
                    is ZipException.WebViewException -> exception.toError().displayableMessage
                    is ZipException.UnknownException -> exception.toError().displayableMessage
                }
                Log.d("[ZipWidget]", "Failure: $error")
                Toast.makeText(
                    context,
                    "Zip tokenisation failed! [${error}]",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }
}