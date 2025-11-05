package com.paydock.sample.feature.checkout.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.components.button.AppButtonVariant
import com.paydock.sample.designsystems.components.sheets.AddressWidgetResult
import com.paydock.sample.designsystems.components.sheets.AddressWidgetSheet
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.checkout.domain.model.Address
import com.paydock.sample.feature.checkout.domain.model.AddressType
import com.paydock.sample.feature.checkout.domain.model.CheckoutStep
import com.paydock.sample.feature.checkout.models.ThreeDSType
import com.paydock.sample.feature.checkout.presentation.CheckoutBackCoordinator
import com.paydock.sample.feature.checkout.presentation.EnhancedCheckoutViewModel
import com.paydock.sample.feature.checkout.ui.components.Checkout3DSBottomSheet
import com.paydock.sample.feature.checkout.ui.steps.InformationStepView
import com.paydock.sample.feature.checkout.ui.steps.PaymentStepView
import com.paydock.sample.feature.checkout.ui.steps.PaymentWidgetView
import com.paydock.sample.feature.account.domain.model.SavedAddress as ProfileSavedAddress
import com.paydock.sample.feature.checkout.domain.model.SavedAddress as CheckoutSavedAddress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCheckoutScreen(
    onDismiss: () -> Unit = {},
    onOrderComplete: (isSuccess: Boolean) -> Unit = {},
    viewModel: EnhancedCheckoutViewModel = hiltViewModel(),
    initialStep: CheckoutStep? = null
) {
    val currentStep by viewModel.currentStep.collectAsState()

    // Listen for order completion and navigate to confirmation screen
    LaunchedEffect(viewModel.orderCompleted, viewModel.orderFailed) {
        if (viewModel.orderCompleted) {
            onOrderComplete(true)
            viewModel.resetCheckout()
        } else if (viewModel.orderFailed) {
            onOrderComplete(false)
            viewModel.resetCheckout()
        }
    }

    LaunchedEffect(initialStep) {
        initialStep?.let { viewModel.setCurrentStep(it) }
        viewModel.loadProfileData()
    }

    // Bind toolbar back handler for checkout route
    CheckoutBackCoordinator.onToolbarBack = {
        if (viewModel.isLoading) {
            true
        } else {
            val steps = CheckoutStep.getAllSteps()
            val idx = steps.indexOf(currentStep)
            if (idx > 0) {
                viewModel.goToPreviousStep()
                true
            } else {
                false
            }
        }
    }

    // Handle system back to mirror toolbar back behavior
    BackHandler {
        val handled = CheckoutBackCoordinator.onToolbarBack?.invoke() == true
        if (!handled) {
            onDismiss()
        }
    }

    // Scroll and footer state hoisted to the composable scope
    val scrollState = rememberScrollState()

    // Sync navigation bar color with payment footer visibility
    val selectedMethodForSystemBar = viewModel.selectedPaymentMethod
    val navBarColor = if (selectedMethodForSystemBar != null) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.background
    }
    val backgroundColor = MaterialTheme.colorScheme.background
    val view = LocalView.current
    val activity = remember(view) { view.context.findActivity() }
    DisposableEffect(selectedMethodForSystemBar, navBarColor, activity) {
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightNavigationBars = navBarColor.luminance() > 0.5f
        }
        onDispose {
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightNavigationBars = backgroundColor.luminance() > 0.5f
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Progress Indicator
        ProgressIndicator(
            currentStep = currentStep,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val contentModifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
            Column(
                modifier = contentModifier
            ) {
                // No animation: render step content directly
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentStep) {
                        CheckoutStep.INFORMATION -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                            ) {
                                InformationStepView(viewModel = viewModel)
                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }

                        CheckoutStep.PAYMENT -> {
                            PaymentStepView(viewModel = viewModel)
                        }
                    }
                }
            }
        }

        // Action Buttons / Fixed footer
        when (currentStep) {
            CheckoutStep.INFORMATION -> ActionButtons(
                viewModel = viewModel,
                currentStep = currentStep
            )

            CheckoutStep.PAYMENT -> PaymentFooter(viewModel = viewModel) { _ -> /* reserved for measurement if needed */ }
        }
    }

    // Alert Dialog
    if (viewModel.showAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAlert() },
            title = { Text(viewModel.alertTitle) },
            text = { Text(viewModel.alertMessage) },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.button_ok),
                    onClick = {
                        viewModel.dismissAlert()
                        if (viewModel.orderCompleted) {
                            onDismiss()
                        }
                    }
                )
            }
        )
    }

    // Address Widget Modal (shared)
    if (viewModel.showAddressWidget) {
        val initial = viewModel.addressWidgetInitialAddress
        val isEditing = initial != null
        AddressWidgetSheet(
            isEditing = isEditing,
            title = when (viewModel.addressWidgetType) {
                AddressType.SHIPPING -> "Shipping Address"
                AddressType.BILLING -> "Billing Address"
            },
            initialBillingAddress = initial?.let {
                BillingAddress(
                    firstName = viewModel.addressWidgetInitialFirstName,
                    lastName = viewModel.addressWidgetInitialLastName,
                    addressLine1 = it.addressLine1,
                    addressLine2 = it.addressLine2,
                    city = it.city,
                    state = it.state,
                    postalCode = it.postalCode,
                    country = it.country
                )
            },
            initialLabel = viewModel.addressWidgetInitialLabel,
            initialIsDefault = true,
            showDefaultToggle = true,
            showAddressTypeChips = true,
            onDismissRequest = { viewModel.hideAddressWidget() },
            onSave = { result: AddressWidgetResult ->
                // Check if we're editing an existing address or creating a new one
                val isEditing = viewModel.editingSavedAddressId != null

                // Create a SavedAddress with existing ID if editing, or generate new ID
                // Store full country name for display; will convert to code when making API requests
                val savedAddress = ProfileSavedAddress(
                    id = viewModel.editingSavedAddressId ?: ProfileSavedAddress.generateId(),
                    label = result.label,
                    firstName = result.billingAddress.firstName ?: "",
                    lastName = result.billingAddress.lastName ?: "",
                    addressLine1 = result.billingAddress.addressLine1 ?: "",
                    addressLine2 = result.billingAddress.addressLine2 ?: "",
                    city = result.billingAddress.city ?: "",
                    state = result.billingAddress.state ?: "",
                    postalCode = result.billingAddress.postalCode ?: "",
                    country = result.billingAddress.country ?: result.billingAddress.countryCode
                    ?: "",
                    isDefault = result.isDefault
                )

                // Update existing address or add new one to profile manager
                if (isEditing) {
                    viewModel.updateAddress(savedAddress)
                } else {
                    viewModel.saveAddress(savedAddress)
                }

                // Convert to checkout SavedAddress
                val checkoutSavedAddress = CheckoutSavedAddress(
                    id = savedAddress.id,
                    label = savedAddress.label,
                    address = Address(
                        addressLine1 = savedAddress.addressLine1,
                        addressLine2 = savedAddress.addressLine2,
                        city = savedAddress.city,
                        state = savedAddress.state,
                        postalCode = savedAddress.postalCode,
                        country = savedAddress.country
                    ),
                    firstName = savedAddress.firstName,
                    lastName = savedAddress.lastName,
                    isDefault = savedAddress.isDefault
                )

                // Select the newly added address
                when (viewModel.addressWidgetType) {
                    AddressType.SHIPPING -> viewModel.selectSavedAddress(
                        checkoutSavedAddress,
                        AddressType.SHIPPING
                    )

                    AddressType.BILLING -> viewModel.selectSavedAddress(
                        checkoutSavedAddress,
                        AddressType.BILLING
                    )
                }

                viewModel.hideAddressWidget()
            }
        )
    }

    // 3DS Bottom Sheet
    val threeDSToken = viewModel.threeDSToken
    val vaultToken = viewModel.vaultToken
    if (!threeDSToken.isNullOrBlank() && !vaultToken.isNullOrBlank()) {
        val bottom3DSSheetState = rememberModalBottomSheetState(
            // This will expand the modal fully based on the size
            skipPartiallyExpanded = true,
            // Prevents dismissing sheet when dragging
            confirmValueChange = { newState ->
                newState != SheetValue.Hidden //  Stop bottom sheet from hiding on outside press
            }
        )
        val isLoading = viewModel.isLoading
        Checkout3DSBottomSheet(
            bottom3DSSheetState = bottom3DSSheetState,
            onDismissRequest = {
                // Handle dismiss (close button click) as 3DS failure/cancellation
                val type = viewModel.threeDSType
                when (type) {
                    ThreeDSType.INTEGRATED -> {
                        viewModel.handleIntegrated3DSResult(
                            Result.failure(Exception("3DS cancelled by user"))
                        )
                    }

                    ThreeDSType.STANDALONE -> {
                        viewModel.handleStandalone3DSResult(
                            Result.failure(Exception("3DS cancelled by user"))
                        )
                    }
                }
            },
            vaultToken = vaultToken,
            threeDSToken = threeDSToken,
            showCloseButton = !isLoading,
            viewModel = viewModel
        )
    }

    // Full-screen blocking loader above everything (including payment footer and 3DS sheet)
    if (viewModel.isLoading) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ProgressIndicator(
    currentStep: CheckoutStep,
    modifier: Modifier = Modifier
) {
    val steps = CheckoutStep.getAllSteps()
    val currentIndex = steps.indexOf(currentStep)

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isSelected = index == currentIndex
            val containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
            val contentColor =
                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .background(
                        color = containerColor,
                        shape = when (index) {
                            0 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                            steps.lastIndex -> RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                            else -> RoundedCornerShape(6.dp)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    viewModel: EnhancedCheckoutViewModel,
    currentStep: CheckoutStep
) {
    Column {
        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Back button (not shown for first step)
            if (currentStep != CheckoutStep.INFORMATION) {
                AppButton(
                    text = stringResource(R.string.button_back),
                    onClick = { viewModel.goToPreviousStep() },
                    variant = AppButtonVariant.Outline,
                    height = 50.dp,
                    modifier = Modifier.weight(1f)
                )
            }

            // Main action button
            AppButton(
                text = viewModel.actionButtonTitle,
                onClick = { viewModel.handleActionButton() },
                enabled = viewModel.canProceed && !viewModel.isLoading,
                isLoading = viewModel.isLoading && currentStep == CheckoutStep.PAYMENT,
                variant = AppButtonVariant.Filled,
                height = 50.dp,
                containerColor = if (viewModel.canProceed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                contentColor = Color.White,
                modifier = Modifier.weight(if (currentStep == CheckoutStep.INFORMATION) 1f else 1f)
            )
        }
    }
}

@Composable
private fun PaymentFooter(
    viewModel: EnhancedCheckoutViewModel,
    onHeightChange: (Dp) -> Unit
) {
    val selectedMethod = viewModel.selectedPaymentMethod
    AnimatedVisibility(
        visible = selectedMethod != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        val density = LocalDensity.current
        Column(modifier = Modifier.fillMaxWidth()) {
            // Full-bleed divider above the rounded surface to avoid corner clipping
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
            )
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .onGloballyPositioned { coords ->
                        onHeightChange(with(density) { coords.size.height.toDp() })
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Crossfade(
                        targetState = selectedMethod,
                        label = "paymentWidgetCrossfade"
                    ) { method ->
                        if (method != null) {
                            // Use key with both method and resetKey to force fresh widget instance
                            // This ensures a new instance when method changes OR when resetKey changes
                            key(method, viewModel.paymentWidgetResetKey) {
                                PaymentWidgetView(
                                    method = method,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewEnhancedCheckoutScreen() {
    SampleTheme {
        EnhancedCheckoutScreen()
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}