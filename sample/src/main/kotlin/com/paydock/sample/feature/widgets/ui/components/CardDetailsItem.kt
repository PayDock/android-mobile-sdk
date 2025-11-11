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
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.domain.error.exceptions.GenericException
import com.paydock.core.domain.error.toError
import com.paydock.core.domain.model.Event
import com.paydock.core.network.dto.error.displayableMessage
import com.paydock.core.presentation.util.WidgetEventDelegate
import com.paydock.feature.card.domain.model.integration.CardDetailsWidgetConfig
import com.paydock.feature.card.domain.model.integration.SaveCardConfig
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.card.presentation.CardDetailsAppearanceDefaults
import com.paydock.feature.card.presentation.CardDetailsWidget
import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.style.StylingViewModel

@Composable
fun CardDetailsItem(context: Context, stylingViewModel: StylingViewModel) {
    val cardDetailsAppearance by stylingViewModel.cardDetailsWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance =
        cardDetailsAppearance ?: CardDetailsAppearanceDefaults.appearance()
    CardDetailsWidget(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        config = CardDetailsWidgetConfig(
            accessToken = BuildConfig.ACCESS_TOKEN_WIDGET,
            gatewayId = BuildConfig.SERVICE_ID_MPGS,
            allowSaveCard = SaveCardConfig(
                privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(
                    privacyPolicyURL = "https://www.google.com"
                )
            ),
            schemeSupport = SupportedSchemeConfig(
                supportedSchemes = setOf(
                    CardType.VISA,
                    CardType.MASTERCARD,
                    CardType.AMEX,
                    CardType.AUSBC,
                    CardType.DINERS,
                    CardType.DISCOVER,
                    CardType.JAPCB,
                    CardType.SOLO,
                    CardType.UNIONPAY
                ),
                enableValidation = true
            )
        ),
        appearance = currentOrDefaultAppearance,
        eventDelegate = object : WidgetEventDelegate {
            override fun widgetEvent(event: Event) {
                Log.d("[CardDetailsWidget Event]", "[type=${event.type}] $event")
            }
        },
        completion = { result ->
            // This breaks down 3 ways to retrieve and handle the result
            // Option 1: Default Result Handler
            result.onSuccess {
                Log.d("[CardDetailsWidget]", "Success: $it")
                Toast.makeText(
                    context,
                    "Tokenised card was successful! [$it]",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }.onFailure { exception: Throwable ->
                if (exception is GenericException) {
                    val error = exception.toError().displayableMessage
                    Log.d("[CardDetailsWidget]", "Failure: $error")
                    Toast.makeText(
                        context,
                        "Tokenised card failed! [${error}]",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else if (exception is CardDetailsException) {
                    val error = when (exception) {
                        is CardDetailsException.TokenisingCardException -> exception.error.displayableMessage
                        is CardDetailsException.ParseException -> exception.toError().displayableMessage
                        is CardDetailsException.UnknownException -> exception.toError().displayableMessage
                    }
                    Log.d("[CardDetailsWidget]", "Failure: $error")
                    Toast.makeText(
                        context,
                        "Tokenised card failed! [${error}]",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

            // Option 2: Use try catch block with getOrThrow extension function
//                        try {
//                            val token: String = result.getOrThrow(CardDetailsException::class)
//                            Log.d("[CardDetailsWidget]", "Success: $token")
//                            context.toast("Tokenised card was successful! [$token]")
//                        } catch (exception: CardDetailsException) {
//                            when (exception) {
//                                is CardDetailsException.TokenisingCardException -> TODO()
//                                is CardDetailsException.UnknownException -> TODO()
//                            }
//                        } catch (e: Exception) {
//                            // Catch generic exceptions
//                            val error = e.toError()
//                            Log.d("[WidgetFailure]", "Failure: ${error.displayableMessage}")
//                        }

            // Option 3: Use onFailure extension function to specify Exception
//                        result.onSuccess {
//                            Log.d("[CardDetailsWidget]", "Success: $it")
//                            context.toast("Tokenised card was successful! [$it]")
//                        }.onFailure(CardDetailsException::class) { exception: CardDetailsException ->
//                                when (exception) {
//                                    is CardDetailsException.TokenisingCardException -> TODO()
//                                    is CardDetailsException.UnknownException -> TODO()
//                                }
//                            }.recoverCatching {
//                                // Catch generic exceptions
//                                val error = it.toError()
//                                Log.d("[CardDetailsWidget]", "Failure: ${error.displayableMessage}")
//                            }
        })
}