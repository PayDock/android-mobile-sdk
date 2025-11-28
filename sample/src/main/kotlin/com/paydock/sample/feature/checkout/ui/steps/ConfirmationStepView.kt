package com.paydock.sample.feature.checkout.ui.steps

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
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.components.button.AppButtonVariant
import com.paydock.sample.feature.checkout.presentation.EnhancedCheckoutViewModel

@Composable
fun ConfirmationStepView(
    viewModel: EnhancedCheckoutViewModel,
    onContinueShopping: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    // Disable system back while on confirmation. Users must use on-screen actions.
    BackHandler(enabled = true) { /* consume back press */ }
    val isSuccess = viewModel.orderCompleted
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isSuccess) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(96.dp)
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .clip(CircleShape)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth(0.4f)
                        .clip(CircleShape)
                        .padding(0.dp)
                        .then(Modifier)
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
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isSuccess) "Payment Confirmation" else "Payment Failed",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isSuccess) "Your payment was successful and your order has been placed." else "Your payment wasn’t completed. You can retry now or cancel and try again later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (isSuccess) {
            AppButton(
                text = "Continue Shopping",
                onClick = { onContinueShopping() },
                modifier = Modifier.fillMaxWidth(),
                variant = AppButtonVariant.Filled
            )
        } else {
            // Failure actions: Retry (Filled) and Cancel (Outline). Cancel keeps cart intact.
            AppButton(
                text = "Retry",
                onClick = { viewModel.setCurrentStep(com.paydock.sample.feature.checkout.domain.model.CheckoutStep.PAYMENT) },
                modifier = Modifier.fillMaxWidth(),
                variant = AppButtonVariant.Filled
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppButton(
                text = "Cancel",
                onClick = { onCancel() },
                modifier = Modifier.fillMaxWidth(),
                variant = AppButtonVariant.Outline
            )
        }
    }
}


