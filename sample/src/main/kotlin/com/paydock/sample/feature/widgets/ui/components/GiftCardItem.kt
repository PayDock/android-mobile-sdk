package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.feature.card.presentation.GiftCardWidget
import com.paydock.sample.BuildConfig

@Composable
fun GiftCardItem(context: Context) {
    GiftCardWidget(
        modifier = Modifier.padding(16.dp),
        accessToken = BuildConfig.WIDGET_ACCESS_TOKEN,
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