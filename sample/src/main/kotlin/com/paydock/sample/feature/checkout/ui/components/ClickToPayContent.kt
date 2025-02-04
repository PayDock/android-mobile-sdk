package com.paydock.sample.feature.checkout.ui.components

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.paydock.sample.feature.checkout.ClickToPayActivity

@Composable
fun ClickToPayComponent(
    isLoading: Boolean,
    accessToken: String,
    resultHandler: (Result<String>) -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val isSuccess = result.data?.getBooleanExtra("isSuccess", false) ?: false
            // Handle the result (success or failure)
            if (isSuccess) {
                val token = result.data?.getStringExtra("token")
                token?.let { resultHandler(Result.success(it)) }
            } else {
                val message = result.data?.getStringExtra("message")
                message?.let { resultHandler(Result.failure(Exception(message))) }
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            // Handle the cancellation
            resultHandler(Result.failure(Exception("Canceled")))
        }
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val intent = Intent(context, ClickToPayActivity::class.java)
            .putExtra("accessToken", accessToken)
        launcher.launch(intent)
    }
}