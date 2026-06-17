package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.feature.threeDS.common.domain.integration.ThreeDSConfig
import com.paydock.feature.threeDS.integrated.presentation.MPGS3dsWidget
import com.paydock.feature.threeDS.integrated.presentation.ui.MPGSThreeDSWidgetAppearanceDefaults
import com.paydock.sample.core.THREE_DS_CARD_ERROR
import com.paydock.sample.core.TOKENISE_CARD_ERROR
import com.paydock.sample.feature.card.CardViewModel
import com.paydock.sample.feature.style.StylingViewModel
import com.paydock.sample.feature.threeDS.presentation.ThreeDSViewModel

@Composable
fun MPGS3dsItem(
    context: Context,
    cardViewModel: CardViewModel = hiltViewModel(),
    threeDSViewModel: ThreeDSViewModel = hiltViewModel(),
    stylingViewModel: StylingViewModel,
) {
    val cardUIState by cardViewModel.stateFlow.collectAsState()
    val threeDSUIState by threeDSViewModel.stateFlow.collectAsState()
    // Use LaunchedEffect to execute the API request and state collection only once
    LaunchedEffect(cardViewModel) {
        cardViewModel.resetResultState()
        cardViewModel.tokeniseCardDetails()
    }
    LaunchedEffect(threeDSViewModel) {
        threeDSViewModel.resetResultState()
    }
    val threeDSAppearance by stylingViewModel.mpgs3dsWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance = threeDSAppearance ?: MPGSThreeDSWidgetAppearanceDefaults.appearance()
    val cardToken = cardUIState.token
    val threeDSToken = threeDSUIState.token
    when {
        !threeDSToken.isNullOrBlank() -> {
            MPGS3dsWidget(
                config = ThreeDSConfig(token = threeDSToken),
                appearance = currentOrDefaultAppearance
            ) { result ->
                result.onSuccess {
                    Log.d("[MPGS3dsWidget]", "Success: $it")
                    Toast.makeText(context, "3DS Result returned [$it]", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    val error = it.toError()
                    Log.d("[MPGS3dsWidget]", "Failure: ${error.displayableMessage}")
                    Toast.makeText(
                        context,
                        "3DS Result failed! [${error.displayableMessage}]",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        !cardToken.isNullOrBlank() -> {
            cardViewModel.resetResultState()
            threeDSViewModel.createMPGS3dsToken(cardToken)
        }

        !cardUIState.error.isNullOrBlank() -> {
            Toast.makeText(context, cardUIState.error ?: TOKENISE_CARD_ERROR, Toast.LENGTH_SHORT)
                .show()
        }

        !threeDSUIState.error.isNullOrBlank() -> {
            Toast.makeText(context, threeDSUIState.error ?: THREE_DS_CARD_ERROR, Toast.LENGTH_SHORT)
                .show()
        }

        cardUIState.isLoading || threeDSUIState.isLoading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
