package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.domain.error.exceptions.GenericException
import com.paydock.core.domain.error.toError
import com.paydock.core.network.dto.error.displayableMessage
import com.paydock.feature.card.domain.model.integration.SaveCardConfig
import com.paydock.feature.card.presentation.CardDetailsWidget
import com.paydock.sample.BuildConfig

@Composable
fun CardDetailsItem(context: Context, accessToken: String) {
    CardDetailsWidget(
        modifier = Modifier.padding(16.dp),
        accessToken = accessToken,
        gatewayId = BuildConfig.GATEWAY_ID,
        allowSaveCard = SaveCardConfig(
            privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(
                privacyPolicyURL = "https://www.google.com"
            )
        ),
        completion = { result ->
            // This breaks down 3 ways to retrieve and handle the result
            // Option 1: Default Result Handler
            result.onSuccess {
                Log.d("[CardDetailsWidget]", "Success: $it")
                Toast.makeText(context, "Tokenised card was successful! [$it]", Toast.LENGTH_SHORT)
                    .show()
            }.onFailure { exception: Throwable ->
                if (exception is GenericException) {
                    val error = exception.toError().displayableMessage
                    Log.d("[CardDetailsWidget]", "Failure: $error")
                    Toast.makeText(context, "Tokenised card failed! [${error}]", Toast.LENGTH_SHORT)
                        .show()
                } else if (exception is CardDetailsException) {
                    val error = when (exception) {
                        is CardDetailsException.TokenisingCardException -> exception.error.displayableMessage
                        is CardDetailsException.UnknownException -> {
                            exception.toError().displayableMessage
                        }
                    }
                    Log.d("[CardDetailsWidget]", "Failure: $error")
                    Toast.makeText(context, "Tokenised card failed! [${error}]", Toast.LENGTH_SHORT)
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