package com.paydock.sample.feature.checkout.ui.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.feature.card.domain.model.integration.GiftCardWidgetConfig
import com.paydock.feature.card.presentation.GiftCardAppearanceDefaults
import com.paydock.feature.card.presentation.GiftCardWidget
import com.paydock.feature.paypal.vault.domain.model.integration.ButtonIcon
import com.paydock.feature.wallet.domain.model.integration.WalletType
import com.paydock.sample.BuildConfig
import com.paydock.sample.R
import com.paydock.sample.core.utils.CurrencyFormatter
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.components.button.AppTextButton
import com.paydock.sample.designsystems.components.sheets.AppBottomSheet
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.checkout.domain.model.PaymentMethod
import com.paydock.sample.feature.checkout.presentation.EnhancedCheckoutViewModel
import com.paydock.sample.feature.checkout.ui.components.AfterpayContent
import com.paydock.sample.feature.checkout.ui.components.CardContent
import com.paydock.sample.feature.checkout.ui.components.ClickToPayComponent
import com.paydock.sample.feature.checkout.ui.components.ColesPayContent
import com.paydock.sample.feature.checkout.ui.components.GooglePayContent
import com.paydock.sample.feature.checkout.ui.components.PayPalContent
import com.paydock.sample.feature.checkout.ui.components.ZipContent
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.shop.data.CartManager
import com.paydock.sample.feature.shop.domain.model.AppliedGiftCard
import com.paydock.sample.feature.shop.domain.model.GiftCard
import com.paydock.sample.feature.shop.domain.model.ShippingOption
import com.paydock.sample.feature.wallet.presentation.WalletViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PaymentStepView(
    viewModel: EnhancedCheckoutViewModel,
    configViewModel: ConfigViewModel = hiltViewModel()
) {
    // Shared cart manager for shipping/gift card sections
    val cartManager = remember { CartManager.shared }
    val globalConfig by configViewModel.globalConfig.collectAsState()
    val currencyCode = globalConfig.currencyCode

    var expanded by remember { mutableStateOf(false) }
    var showGiftCardEntry by remember { mutableStateOf(false) }
    var giftCardSheetKey by remember { mutableIntStateOf(0) }

    val sectionSpacing = 20.dp
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
        contentPadding = PaddingValues(bottom = 12.dp),
        modifier = Modifier.testTag("payment_step_view")
    ) {
        // Title (scrolls with content)
        item {
            Text(
                text = stringResource(R.string.label_payment_method),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("payment_method_title")
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("payment_method_dropdown"),
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
                                        "ic_zip_widget" -> R.drawable.ic_zip_widget
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
                                        ?: stringResource(R.string.label_select_payment_method),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("payment_method_selected_text")
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.testTag("payment_method_list")
                ) {
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
                currencyCode = currencyCode,
                onShowGiftCardEntry = {
                    giftCardSheetKey++
                    showGiftCardEntry = true
                })
        }

        item {
            HorizontalDivider()
        }

        item {
            ShippingSection(
                selectedShipping = cartManager.selectedShipping.collectAsState().value,
                cartManager = cartManager,
                currencyCode = currencyCode
            )
        }

        item {
            HorizontalDivider()
        }

        item {
            ReviewSummary(currencyCode = currencyCode)
        }
    }

    if (showGiftCardEntry) {
        // Use a unique key to force fresh state each time the sheet is opened
        key(giftCardSheetKey) {
            GiftCardEntryView(
                cartManager = cartManager, onDismiss = { showGiftCardEntry = false }
            )
        }
    }
}

@Composable
private fun ReviewSummary(currencyCode: String) {
    val cartManager = CartManager.shared
    val appliedGiftCards = cartManager.appliedGiftCards.collectAsState().value
    // Collect to trigger recomposition when these change
    cartManager.selectedShipping.collectAsState().value
    cartManager.cartItems.collectAsState().value
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.testTag("review_summary_section")
    ) {
        Text(
            text = "Order Summary",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("order_summary_title")
        )
        // Reuse minimal summary similar to ReviewStepView
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("order_summary_subtotal_row"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subtotal",
                modifier = Modifier
                    .weight(1f)
                    .testTag("order_summary_subtotal_label"),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = CurrencyFormatter.format(cartManager.subtotal, currencyCode),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("order_summary_subtotal_amount")
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("order_summary_shipping_row"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shipping",
                modifier = Modifier
                    .weight(1f)
                    .testTag("order_summary_shipping_label"),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = if (cartManager.shippingCost == 0.0) "Free" else CurrencyFormatter.format(
                    cartManager.shippingCost,
                    currencyCode
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("order_summary_shipping_amount")
            )
        }
        if (appliedGiftCards.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_summary_discounts_row"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discounts",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("order_summary_discounts_label"),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "-${
                        CurrencyFormatter.format(
                            cartManager.totalGiftCardAmount,
                            currencyCode
                        )
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("order_summary_discounts_amount")
                )
            }
        }
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("order_summary_total_row"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total",
                modifier = Modifier
                    .weight(1f)
                    .testTag("order_summary_total_label"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = CurrencyFormatter.format(cartManager.totalPrice, currencyCode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("order_summary_total_amount")
            )
        }
    }
}

@Composable
private fun GiftCardSection(
    appliedGiftCards: List<AppliedGiftCard>,
    cartManager: CartManager,
    currencyCode: String,
    onShowGiftCardEntry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gift_card_section"),
        colors = CardDefaults.cardColors(
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
                    text = stringResource(R.string.label_gift_cards),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                AppTextButton(
                    text = stringResource(R.string.label_add_gift_card),
                    onClick = onShowGiftCardEntry,
                    modifier = Modifier.testTag("add_gift_card_button")
                )
            }

            if (cartManager.hasGiftCards) {
                appliedGiftCards.forEach { appliedGiftCard ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("applied_gift_card_${appliedGiftCard.giftCard.id}"),
                        colors = CardDefaults.cardColors(
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
                                    text = "Applied: ${
                                        CurrencyFormatter.format(
                                            appliedGiftCard.appliedAmount,
                                            currencyCode
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            AppTextButton(
                                text = stringResource(R.string.button_remove),
                                onClick = { cartManager.removeGiftCard(appliedGiftCard) },
                                modifier = Modifier.testTag("remove_gift_card_${appliedGiftCard.giftCard.id}")
                            )
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
    val configViewModel: ConfigViewModel = hiltViewModel()
    val globalConfig by configViewModel.globalConfig.collectAsState()
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Create a fresh ViewModelStoreOwner for each instance to ensure fresh ViewModel
    val viewModelStoreOwner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = ViewModelStore()
        }
    }

    val handleDismiss = {
        if (!isLoading) {
            // Clear the ViewModel store on dismiss to prevent memory leaks
            viewModelStoreOwner.viewModelStore.clear()
            onDismiss()
        }
    }

    AppBottomSheet(
        title = stringResource(R.string.label_add_gift_card),
        trailingContent = {
            AppTextButton(
                text = "Cancel",
                onClick = handleDismiss,
                enabled = !isLoading
            )
        },
        skipPartiallyExpanded = false,
        expandOnOpen = false,
        allowOutsideDismiss = false,
        onDismissRequest = handleDismiss
    ) {
        // Provide fresh ViewModelStoreOwner to ensure GiftCardWidget gets a fresh ViewModel
        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 0.dp, end = 0.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_enter_gift_card_details),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    GiftCardWidget(
                        config = GiftCardWidgetConfig(
                            accessToken = BuildConfig.ACCESS_TOKEN_WIDGET, storePin = false
                        ),
                        appearance = GiftCardAppearanceDefaults.appearance().copy(
                            actionButton = ButtonAppearanceDefaults.outlineButtonAppearance().copy(
                                text = stringResource(R.string.button_add),
                                icon = ButtonIcon.Vector(Icons.Filled.Add)
                            )
                        ),
                        loadingDelegate = object : WidgetLoadingDelegate {
                            override fun widgetLoadingDidStart() {
                                isLoading = true
                            }

                            override fun widgetLoadingDidFinish() {
                                isLoading = false
                            }
                        },
                        completion = { result ->
                            result.onSuccess { giftCardResult ->
                                handleGiftCardResult(giftCardResult, cartManager) { message ->
                                    alertMessage = message
                                    showAlert = true
                                    if (message.contains("successfully")) {
                                        // Clear the ViewModel store before dismissing
                                        viewModelStoreOwner.viewModelStore.clear()
                                        onDismiss()
                                    }
                                }
                            }.onFailure { error ->
                                alertMessage = error.message ?: "An error occurred"
                                showAlert = true
                            }
                        })
                }

                // Loading overlay - overlays the content without affecting layout
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text(stringResource(R.string.label_gift_cards)) },
            text = { Text(alertMessage) },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.button_ok),
                    onClick = { showAlert = false }
                )
            })
    }
}

@Composable
private fun ShippingSection(
    selectedShipping: ShippingOption,
    cartManager: CartManager,
    currencyCode: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shipping_section"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.label_shipping_options),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ShippingOption.getAllOptions().forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { cartManager.setSelectedShipping(option) }
                        .padding(vertical = 8.dp)
                        .testTag(
                            "shipping_option_${
                                option.shippingNmae.replace(" ", "_").lowercase()
                            }"
                        ),
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
                        text = if (option.price == 0.0) "Free" else CurrencyFormatter.format(
                            option.price,
                            currencyCode
                        ),
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
            .clickable { onSelect() }
            .testTag("payment_method_option_${method.name.lowercase()}"),
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
                "ic_zip_widget" -> R.drawable.ic_zip_widget
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

    // Get customer data from checkout view model for wallet requests
    val customerData = remember(
        viewModel.contactInfo,
        viewModel.shippingAddress,
        viewModel.billingAddress
    ) {
        viewModel.getCustomerDataForWallet()
    }

    // Create a unique NavHost with a key to force fresh ViewModelStore
    // Each time resetKey changes, we get a completely new navigation graph with fresh ViewModels
    val uniqueNavKey = "${method.name}_${viewModel.paymentWidgetResetKey}"

    key(uniqueNavKey) {
        val navController = rememberNavController()

        // NavHost provides both ViewModelStoreOwner and SavedStateRegistryOwner
        NavHost(
            navController = navController,
            startDestination = "payment_widget",
            modifier = Modifier
        ) {
            composable("payment_widget") {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    when (method) {
                        PaymentMethod.CARD -> {
                            CardContent(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = true,
                                loadingDelegate = object : WidgetLoadingDelegate {
                                    override fun widgetLoadingDidStart() {
                                        viewModel.setIsLoading(true)
                                    }

                                    override fun widgetLoadingDidFinish() {
                                        viewModel.setIsLoading(false)
                                    }
                                },
                                resultHandler = { result -> viewModel.handleCardResult(result) }
                            )
                        }

                        PaymentMethod.GOOGLE_PAY -> {
                            GooglePayContent(
                                modifier = Modifier.fillMaxWidth(),
                                loadingDelegate = object : WidgetLoadingDelegate {
                                    override fun widgetLoadingDidStart() {
                                        viewModel.setIsLoading(true)
                                    }

                                    override fun widgetLoadingDidFinish() {
                                        viewModel.setIsLoading(false)
                                    }
                                },
                                resultHandler = { result ->
                                    viewModel.handleGooglePayResult(result)
                                }
                            )
                        }

                        PaymentMethod.PAYPAL -> {
                            PayPalContent(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = true,
                                tokenHandler = walletViewModel.getWalletTokenResultCallback(
                                    walletType = WalletType.PAY_PAL,
                                    customerData = customerData,
                                    useGlobalConfig = false // Checkout uses BuildConfig defaults
                                ),
                                loadingDelegate = object : WidgetLoadingDelegate {
                                    override fun widgetLoadingDidStart() {
                                        viewModel.setIsLoading(true)
                                    }

                                    override fun widgetLoadingDidFinish() {
                                        viewModel.setIsLoading(false)
                                    }
                                },
                                resultHandler = { result ->
                                    result.onSuccess { chargeResponse ->
                                        viewModel.setThePaymentToken(
                                            chargeResponse.resource.data?.id ?: "paypal_success"
                                        )
                                        viewModel.placeOrder()
                                    }
                                    result.onFailure { _ -> viewModel.routeToFailure() }
                                }
                            )
                        }

                        PaymentMethod.AFTERPAY -> {
                            AfterpayContent(
                                modifier = Modifier.fillMaxWidth(),
                                tokenHandler = walletViewModel.getWalletTokenResultCallback(
                                    walletType = WalletType.AFTER_PAY,
                                    customerData = customerData,
                                    useGlobalConfig = false // Checkout uses BuildConfig defaults
                                ),
                                loadingDelegate = object : WidgetLoadingDelegate {
                                    override fun widgetLoadingDidStart() {
                                        viewModel.setIsLoading(true)
                                    }

                                    override fun widgetLoadingDidFinish() {
                                        viewModel.setIsLoading(false)
                                    }
                                },
                                resultHandler = { result ->
                                    result.onSuccess { chargeResponse ->
                                        viewModel.setThePaymentToken(
                                            chargeResponse.resource.data?.id ?: "afterpay_success"
                                        )
                                        viewModel.placeOrder()
                                    }
                                    result.onFailure { _ -> viewModel.routeToFailure() }
                                }
                            )
                        }

                        PaymentMethod.CLICK_TO_PAY -> {
                            ClickToPayComponent(
                                resultHandler = { result -> viewModel.handleClickToPayResult(result) }
                            )
                        }

                        PaymentMethod.COLES_PAY -> {
                            ColesPayContent(
                                modifier = Modifier.fillMaxWidth(),
                                tokenHandler = walletViewModel.getWalletTokenResultCallback(
                                    walletType = WalletType.COLES_PAY,
                                    customerData = customerData,
                                    useGlobalConfig = false // Checkout uses BuildConfig defaults
                                ),
                                loadingDelegate = object : WidgetLoadingDelegate {
                                    override fun widgetLoadingDidStart() {
                                        viewModel.setIsLoading(true)
                                    }

                                    override fun widgetLoadingDidFinish() {
                                        viewModel.setIsLoading(false)
                                    }
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

                        PaymentMethod.ZIP -> {
                            ZipContent(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = true,
                                loadingDelegate = object : WidgetLoadingDelegate {
                                    override fun widgetLoadingDidStart() {
                                        viewModel.setIsLoading(true)
                                    }

                                    override fun widgetLoadingDidFinish() {
                                        viewModel.setIsLoading(false)
                                    }
                                },
                                viewModel = viewModel,
                                resultHandler = { result -> viewModel.handleZipResult(result) }
                            )
                        }
                    }
                }
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