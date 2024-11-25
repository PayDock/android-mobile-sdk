package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.feature.card.presentation.GiftCardWidget

@Composable
fun GiftCardItem(context: Context, accessToken: String) {
    GiftCardWidget(
        accessToken = accessToken,
        storePin = true, completion = { result ->
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