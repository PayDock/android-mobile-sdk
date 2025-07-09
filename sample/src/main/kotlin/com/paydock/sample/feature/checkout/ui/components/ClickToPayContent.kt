package com.paydock.sample.feature.checkout.ui.components

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.feature.checkout.ClickToPayActivity

@Composable
fun ClickToPayComponent(
    resultHandler: (Result<String>) -> Unit,
) {
    // Get the launch function by calling our modified composable
    val launchClickToPay = rememberClickToPayLauncher(
        resultHandler = resultHandler
    )
    AppButton(
        text = stringResource(R.string.label_checkout_with_click_to_pay),
        onClick = {
            launchClickToPay() // Call the function to launch the activity
        },
        modifier = Modifier.fillMaxWidth()
    )
}


/**
 * A composable that sets up the launcher for the ClickToPayActivity
 * and provides a function to trigger its launch.
 *
 * @param resultHandler Callback to handle the result from ClickToPayActivity.
 * @return A lambda function `() -> Unit` that, when called, will launch the ClickToPayActivity.
 */
@Composable
fun rememberClickToPayLauncher(
    resultHandler: (Result<String>) -> Unit,
): () -> Unit { // Return type is a function that takes no args and returns Unit
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult -> // Explicitly type result for clarity
        if (result.resultCode == Activity.RESULT_OK) {
            val isSuccess = result.data?.getBooleanExtra("isSuccess", false) ?: false
            if (isSuccess) {
                val token = result.data?.getStringExtra("token")
                token?.let { resultHandler(Result.success(it)) }
                    ?: resultHandler(Result.failure(Exception("Token not found in result")))
            } else {
                val message = result.data?.getStringExtra("message")
                resultHandler(Result.failure(Exception(message ?: "Click to Pay failed")))
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            // Check for specific cancellation message if ClickToPayActivity provides one
            val message = result.data?.getStringExtra("message")
            resultHandler(Result.failure(Exception(message ?: "Click to Pay Canceled by user")))
        } else {
            // Handle other potential result codes if necessary
            resultHandler(Result.failure(Exception("Click to Pay failed with unknown result code: ${result.resultCode}")))
        }
    }

    // Return a lambda that will launch the activity when called
    return {
        val intent = Intent(context, ClickToPayActivity::class.java)
        // You can add extras to the intent here if ClickToPayActivity needs them
        // intent.putExtra("YOUR_EXTRA_KEY", "your_value")
        launcher.launch(intent)
    }
}