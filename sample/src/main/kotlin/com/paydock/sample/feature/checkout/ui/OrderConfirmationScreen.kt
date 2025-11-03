package com.paydock.sample.feature.checkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.components.button.AppButtonVariant
import com.paydock.sample.designsystems.theme.SampleTheme

/**
 * Standalone Order Confirmation Screen
 * Shows success or failure state after checkout completion
 *
 * @param isSuccess Whether the order was successful
 * @param onContinueShopping Callback for successful orders to return to shop
 * @param onRetryCheckout Callback for failed orders to retry checkout
 * @param onCancel Callback for failed orders to cancel and return to shop
 */
@Composable
fun OrderConfirmationScreen(
    isSuccess: Boolean,
    onContinueShopping: () -> Unit = {},
    onRetryCheckout: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    // Always consume system back. Users must use on-screen buttons.
    BackHandler(enabled = true) { }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isSuccess) {
            // Success State
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth(0.4f)
                        .clip(CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.height(64.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Payment Successful!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your payment was successful and your order has been placed.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            AppButton(
                text = "Continue Shopping",
                onClick = onContinueShopping,
                modifier = Modifier.fillMaxWidth(),
                variant = AppButtonVariant.Filled
            )
        } else {
            // Failure State
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth(0.4f)
                        .clip(CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.height(64.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Payment Failed",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your payment wasn't completed. You can retry now or cancel and try again later.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Retry button (primary action)
            AppButton(
                text = "Retry Payment",
                onClick = onRetryCheckout,
                modifier = Modifier.fillMaxWidth(),
                variant = AppButtonVariant.Filled
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Cancel button (secondary action)
            AppButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                variant = AppButtonVariant.Outline
            )
        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewOrderConfirmationScreenSuccess() {
    SampleTheme {
        OrderConfirmationScreen(
            isSuccess = true,
            onContinueShopping = {}
        )
    }
}

@PreviewLightDark
@Composable
internal fun PreviewOrderConfirmationScreenFailure() {
    SampleTheme {
        OrderConfirmationScreen(
            isSuccess = false,
            onRetryCheckout = {},
            onCancel = {}
        )
    }
}

