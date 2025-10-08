package com.paydock.sample.feature.checkout.ui.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.afterpay.domain.model.integration.AfterpaySDKConfig
import com.paydock.feature.afterpay.presentation.AfterpayAppearanceDefaults
import com.paydock.feature.afterpay.presentation.AfterpayWidget
import com.paydock.feature.card.domain.model.integration.CardDetailsWidgetConfig
import com.paydock.feature.card.domain.model.integration.GiftCardWidgetConfig
import com.paydock.feature.card.domain.model.integration.SaveCardConfig
import com.paydock.feature.card.presentation.CardDetailsAppearanceDefaults
import com.paydock.feature.card.presentation.CardDetailsWidget
import com.paydock.feature.card.presentation.GiftCardWidget
import com.paydock.feature.colespay.integration.ColesPayWidgetConfig
import com.paydock.feature.colespay.presentation.ColesPayWidget
import com.paydock.feature.colespay.presentation.ColesPayWidgetAppearanceDefaults
import com.paydock.feature.googlepay.domain.model.GooglePayWidgetConfig
import com.paydock.feature.googlepay.presentation.GooglePayAppearanceDefaults
import com.paydock.feature.googlepay.presentation.GooglePayWidget
import com.paydock.feature.googlepay.util.PaymentsUtil
import com.paydock.feature.paypal.checkout.domain.model.integration.PayPalWidgetConfig
import com.paydock.feature.paypal.checkout.presentation.PayPalAppearanceDefaults
import com.paydock.feature.paypal.checkout.presentation.PayPalWidget
import com.paydock.feature.src.domain.model.integration.ClickToPayWidgetConfig
import com.paydock.feature.src.presentation.ClickToPayAppearanceDefaults
import com.paydock.feature.src.presentation.ClickToPayWidget
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paydock.feature.wallet.domain.model.integration.WalletType
import com.paydock.sample.BuildConfig
import com.paydock.sample.R
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_COUNTRY_CODE
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.core.COUNTRY_CODE_LIST
import com.paydock.sample.core.MERCHANT_NAME
import com.paydock.sample.designsystems.components.button.AppTextButton
import com.paydock.sample.designsystems.components.sheets.AppBottomSheet
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.checkout.domain.model.PaymentMethod
import com.paydock.sample.feature.checkout.presentation.EnhancedCheckoutViewModel
import com.paydock.sample.feature.checkout.ui.components.ClickToPayComponent
import com.paydock.sample.feature.checkout.ui.components.GooglePayContent
import com.paydock.sample.feature.checkout.ui.components.AfterpayContent
import com.paydock.sample.feature.checkout.ui.components.ColesPayContent
import com.paydock.sample.feature.checkout.ui.components.PayPalContent
import com.paydock.sample.feature.checkout.ui.components.CardContent
import com.paydock.sample.feature.shop.data.CartManager
import com.paydock.sample.feature.shop.domain.model.AppliedGiftCard
import com.paydock.sample.feature.shop.domain.model.GiftCard
import com.paydock.sample.feature.shop.domain.model.ShippingOption
import com.paydock.sample.feature.wallet.presentation.WalletViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.paydock.sample.feature.style.StylingViewModel
import com.paydock.sample.feature.checkout.ui.components.GooglePayContent
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PaymentStepView(
    viewModel: EnhancedCheckoutViewModel
) {
    // Shared cart manager for shipping/gift card sections
    val cartManager = remember { CartManager.shared }
    val walletViewModel: WalletViewModel = hiltViewModel()

    var expanded by remember { mutableStateOf(false) }
    var showGiftCardEntry by remember { mutableStateOf(false) }

    val sectionSpacing = 20.dp
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        // Title (scrolls with content)
        item {
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Sticky payment method section (dropdown only). Title scrolls.
        stickyHeader {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Padded dropdown card (no extra top padding; list spacing already applies)
                    Column(
                        modifier = Modifier
                            .padding(bottom = sectionSpacing)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = !expanded }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                viewModel.selectedPaymentMethod?.let { method ->
                                    val iconRes = when (method.iconRes) {
                                        "ic_card_filled" -> R.drawable.ic_card_filled
                                        "ic_google_pay_default" -> R.drawable.ic_google_pay_default
                                        "ic_paypal" -> R.drawable.ic_paypal
                                        "ic_afterpay" -> R.drawable.ic_afterpay
                                        "ic_src" -> R.drawable.ic_src
                                        "ic_coles_pay" -> R.drawable.ic_coles_pay
                                        else -> R.drawable.ic_card_filled
                                    }
                                    Image(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = method.displayName,
                                        modifier = Modifier.size(width = 40.dp, height = 24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Text(
                                    text = viewModel.selectedPaymentMethod?.displayName
                                        ?: "Select a payment method",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider()
        }

        // Expanded list lives as regular item(s) so it scrolls with content
        if (expanded) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PaymentMethod.getAllMethods().forEach { method ->
                        PaymentMethodRow(
                            method = method,
                            isSelected = viewModel.selectedPaymentMethod == method,
                            onSelect = {
                                viewModel.setPaymentMethod(method)
                                expanded = false
                            })
                    }
                }
            }
            item { HorizontalDivider() }
        }

        item {
            GiftCardSection(
                appliedGiftCards = cartManager.appliedGiftCards.collectAsState().value,
                cartManager = cartManager,
                onShowGiftCardEntry = { showGiftCardEntry = true })
        }

        item {
            HorizontalDivider()
        }

        item {
            ShippingSection(
                selectedShipping = cartManager.selectedShipping.collectAsState().value,
                cartManager = cartManager
            )
        }

        item {
            HorizontalDivider()
        }

        item {
            ReviewSummary()
        }
    }

    if (showGiftCardEntry) {
        GiftCardEntryView(
            cartManager = cartManager, onDismiss = { showGiftCardEntry = false }
        )
    }
}

@Composable
private fun ReviewSummary() {
    val cartManager = CartManager.shared
    val appliedGiftCards = cartManager.appliedGiftCards.collectAsState().value
    // Collect to trigger recomposition when these change
    cartManager.selectedShipping.collectAsState().value
    cartManager.cartItems.collectAsState().value
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Order Summary",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        // Reuse minimal summary similar to ReviewStepView
        HorizontalDivider()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Subtotal",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(text = cartManager.formattedSubtotal, style = MaterialTheme.typography.bodyMedium)
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Shipping",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = cartManager.formattedShippingCost,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (appliedGiftCards.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discounts",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = cartManager.formattedTotalGiftCardAmount,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        HorizontalDivider()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Total",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = cartManager.formattedTotal,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun GiftCardSection(
    appliedGiftCards: List<AppliedGiftCard>,
    cartManager: CartManager,
    onShowGiftCardEntry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gift Cards",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                AppTextButton(text = "Add Gift Card", onClick = onShowGiftCardEntry)
            }

            if (cartManager.hasGiftCards) {
                appliedGiftCards.forEach { appliedGiftCard ->
                    Card(
                        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = appliedGiftCard.giftCard.maskedCardNumber,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Applied: ${appliedGiftCard.formattedAppliedAmount}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            AppTextButton(
                                text = "Remove",
                                onClick = { cartManager.removeGiftCard(appliedGiftCard) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GiftCardEntryView(
    cartManager: CartManager, onDismiss: () -> Unit
) {
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    AppBottomSheet(
        title = "Add Gift Card",
        trailingContent = { AppTextButton(text = "Cancel", onClick = onDismiss) },
        skipPartiallyExpanded = false,
        expandOnOpen = false,
        allowOutsideDismiss = true,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 0.dp, end = 0.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Enter your gift card details to apply it to your order",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            GiftCardWidget(
                config = GiftCardWidgetConfig(
                    accessToken = BuildConfig.WIDGET_ACCESS_TOKEN, storePin = false
                ), completion = { result ->
                    result.onSuccess { giftCardResult ->
                        handleGiftCardResult(giftCardResult, cartManager) { message ->
                            alertMessage = message
                            showAlert = true
                            if (message.contains("successfully")) {
                                onDismiss()
                            }
                        }
                    }.onFailure { error ->
                        alertMessage = error.message ?: "An error occurred"
                        showAlert = true
                    }
                })
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text("Gift Card") },
            text = { Text(alertMessage) },
            confirmButton = {
                androidx.compose.material3.Button(onClick = { showAlert = false }) {
                    Text("OK")
                }
            })
    }
}

@Composable
private fun ShippingSection(
    selectedShipping: ShippingOption, cartManager: CartManager
) {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Shipping Options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ShippingOption.getAllOptions().forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { cartManager.setSelectedShipping(option) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (selectedShipping == option) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = if (selectedShipping == option) "Selected" else "Not selected",
                        tint = if (selectedShipping == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = option.shippingNmae,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = option.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = option.formattedPrice,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun handleGiftCardResult(
    result: Any, cartManager: CartManager, onResult: (String) -> Unit
) {
    val giftCard = GiftCard(
        id = "gc_${System.currentTimeMillis()}",
        cardNumber = "1234567890123456",
        balance = 25.0,
        isActive = true
    )
    if (cartManager.applyGiftCard(giftCard)) {
        onResult("Gift card successfully applied!")
    } else {
        onResult("This gift card has already been applied or your order total is already covered.")
    }
}

@Composable
private fun PaymentMethodRow(
    method: PaymentMethod,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Payment method icon
            val iconRes = when (method.iconRes) {
                "ic_card_filled" -> R.drawable.ic_card_filled
                "ic_google_pay_default" -> R.drawable.ic_google_pay_default
                "ic_paypal" -> R.drawable.ic_paypal
                "ic_afterpay" -> R.drawable.ic_afterpay
                "ic_src" -> R.drawable.ic_src
                "ic_coles_pay" -> R.drawable.ic_coles_pay
                else -> R.drawable.ic_card_filled
            }
            
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = method.displayName,
                modifier = Modifier.size(width = 40.dp, height = 24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = method.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PaymentWidgetView(
    method: PaymentMethod,
    viewModel: EnhancedCheckoutViewModel
    ) {
        val walletViewModel: WalletViewModel = hiltViewModel()
        val stylingViewModel: StylingViewModel = hiltViewModel()
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            when (method) {
                PaymentMethod.CARD -> {
                    CardContent(
                        stylingViewModel = stylingViewModel,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true,
                        loadingDelegate = object : WidgetLoadingDelegate {
                            override fun widgetLoadingDidStart() { viewModel.setIsLoading(true) }
                            override fun widgetLoadingDidFinish() { viewModel.setIsLoading(false) }
                        },
                        resultHandler = { result -> viewModel.handleCardResult(result) }
                    )
                }
                
                PaymentMethod.GOOGLE_PAY -> {
                    GooglePayContent(
                        stylingViewModel = stylingViewModel,
                        modifier = Modifier.fillMaxWidth(),
                        tokenHandler = walletViewModel.getWalletTokenResultCallback(WalletType.GOOGLE),
                        loadingDelegate = object : WidgetLoadingDelegate {
                            override fun widgetLoadingDidStart() { viewModel.setIsLoading(true) }
                            override fun widgetLoadingDidFinish() { viewModel.setIsLoading(false) }
                        },
                        resultHandler = { result ->
                            result.onSuccess { chargeResponse ->
                                viewModel.setThePaymentToken(chargeResponse.resource.data?.id ?: "google_pay_success")
                                viewModel.placeOrder()
                            }
                            result.onFailure { _ -> viewModel.routeToFailure() }
                        }
                    )
                }
                
                PaymentMethod.PAYPAL -> {
                    PayPalContent(
                        stylingViewModel = stylingViewModel,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true,
                        tokenHandler = walletViewModel.getWalletTokenResultCallback(WalletType.PAY_PAL),
                        loadingDelegate = object : WidgetLoadingDelegate {
                            override fun widgetLoadingDidStart() { viewModel.setIsLoading(true) }
                            override fun widgetLoadingDidFinish() { viewModel.setIsLoading(false) }
                        },
                        resultHandler = { result ->
                            result.onSuccess { chargeResponse ->
                                viewModel.setThePaymentToken(chargeResponse.resource.data?.id ?: "paypal_success")
                                viewModel.placeOrder()
                            }
                            result.onFailure { _ -> viewModel.routeToFailure() }
                        }
                    )
                }
                
                PaymentMethod.AFTERPAY -> {
                    AfterpayContent(
                        stylingViewModel = stylingViewModel,
                        modifier = Modifier.fillMaxWidth(),
                        tokenHandler = walletViewModel.getWalletTokenResultCallback(WalletType.AFTER_PAY),
                        loadingDelegate = object : WidgetLoadingDelegate {
                            override fun widgetLoadingDidStart() { viewModel.setIsLoading(true) }
                            override fun widgetLoadingDidFinish() { viewModel.setIsLoading(false) }
                        },
                        resultHandler = { result ->
                            result.onSuccess { chargeResponse ->
                                viewModel.setThePaymentToken(chargeResponse.resource.data?.id ?: "afterpay_success")
                                viewModel.placeOrder()
                            }
                            result.onFailure { _ -> viewModel.routeToFailure() }
                        }
                    )
                }
                
                PaymentMethod.CLICK_TO_PAY -> {
                    ClickToPayComponent(
                        resultHandler = { result ->
                            result.onSuccess { token ->
                                viewModel.setThePaymentToken(token)
                                viewModel.placeOrder()
                            }
                            result.onFailure { _ ->
                                viewModel.routeToFailure()
                            }
                        }
                    )
                }
                
                PaymentMethod.COLES_PAY -> {
                    ColesPayContent(
                        stylingViewModel = stylingViewModel,
                        modifier = Modifier.fillMaxWidth(),
                        tokenHandler = walletViewModel.getWalletTokenResultCallback(WalletType.COLES_PAY),
                        loadingDelegate = object : WidgetLoadingDelegate {
                            override fun widgetLoadingDidStart() { viewModel.setIsLoading(true) }
                            override fun widgetLoadingDidFinish() { viewModel.setIsLoading(false) }
                        },
                        resultHandler = { result ->
                            result.onSuccess { token ->
                                viewModel.setThePaymentToken(token)
                                viewModel.placeOrder()
                            }
                            result.onFailure { _ -> viewModel.routeToFailure() }
                        }
                    )
                }
        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewPaymentStepView() {
    SampleTheme {
        // PaymentStepView(viewModel = EnhancedCheckoutViewModel())
    }
} 