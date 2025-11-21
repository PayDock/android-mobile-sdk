package com.paydock.sample.feature.main

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.paydock.feature.address.presentation.AddressDetailsAppearanceDefaults
import com.paydock.feature.address.presentation.AddressDetailsWidgetAppearance
import com.paydock.feature.afterpay.presentation.AfterpayAppearanceDefaults
import com.paydock.feature.afterpay.presentation.AfterpayWidgetAppearance
import com.paydock.feature.card.presentation.CardDetailsAppearanceDefaults
import com.paydock.feature.card.presentation.CardDetailsWidgetAppearance
import com.paydock.feature.card.presentation.GiftCardAppearanceDefaults
import com.paydock.feature.card.presentation.GiftCardWidgetAppearance
import com.paydock.feature.colespay.presentation.ColesPayWidgetAppearance
import com.paydock.feature.colespay.presentation.ColesPayWidgetAppearanceDefaults
import com.paydock.feature.googlepay.presentation.GooglePayAppearanceDefaults
import com.paydock.feature.googlepay.presentation.GooglePayWidgetAppearance
import com.paydock.feature.paypal.checkout.presentation.PayPalAppearanceDefaults
import com.paydock.feature.paypal.checkout.presentation.PayPalWidgetAppearance
import com.paydock.feature.paypal.vault.presentation.PayPalPaymentSourceAppearanceDefaults
import com.paydock.feature.paypal.vault.presentation.PayPalPaymentSourceWidgetAppearance
import com.paydock.feature.src.presentation.ClickToPayAppearanceDefaults
import com.paydock.feature.src.presentation.ClickToPayWidgetAppearance
import com.paydock.feature.threeDS.common.presentation.ui.ThreeDSAppearanceDefaults
import com.paydock.feature.threeDS.common.presentation.ui.ThreeDSWidgetAppearance
import com.paydock.sample.designsystems.components.CenterAppTopBar
import com.paydock.sample.designsystems.components.navigation.BottomNavigation
import com.paydock.sample.designsystems.components.navigation.NavigationGraph
import com.paydock.sample.designsystems.components.navigation.getRouteTitle
import com.paydock.sample.designsystems.components.navigation.showBackButton
import com.paydock.sample.designsystems.components.navigation.showTitle
import com.paydock.sample.designsystems.theme.AppTheme
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.shop.data.CartManager
import com.paydock.sample.feature.style.StylingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            enableEdgeToEdge(
                statusBarStyle = getSystemBarStyle(),
                navigationBarStyle = getSystemBarStyle()
            )

            var currentAppTheme by rememberSaveable(
                stateSaver = Saver<AppTheme, String>(
                    save = { it.name },
                    restore = { AppTheme.valueOf(it) }
                )
            ) { mutableStateOf(AppTheme.SYSTEM) }

            val isActuallyDark = when (currentAppTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            SampleTheme(isActuallyDark) {
                MainScreenView(
                    isActuallyDark = isActuallyDark,
                    onThemeSelected = { newTheme -> currentAppTheme = newTheme }
                )
            }
        }
    }
}

@Composable
private fun getSystemBarStyle(): SystemBarStyle = SystemBarStyle.run {
    val color = Color.Transparent.toArgb()
    if (isSystemInDarkTheme()) {
        dark(color)
    } else {
        light(color, color)
    }
}

@Composable
fun MainScreenView(
    stylingViewModel: StylingViewModel = hiltViewModel(),
    isActuallyDark: Boolean,
    onThemeSelected: (AppTheme) -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()// Hoist these states outside the composable function
    val actionBarDetails = rememberActionBarDetails(navController, context)

    // *** Call @Composable defaults here, in the Composable scope ***
    // These will re-evaluate if MainScreenView recomposes due to isActuallyDark changing
    val addressSdkDefaults: AddressDetailsWidgetAppearance =
        AddressDetailsAppearanceDefaults.appearance()
    val cardSdkDefaults: CardDetailsWidgetAppearance = CardDetailsAppearanceDefaults.appearance()
    val giftCardSdkDefaults: GiftCardWidgetAppearance = GiftCardAppearanceDefaults.appearance()
    val clickToPaySdkDefaults: ClickToPayWidgetAppearance =
        ClickToPayAppearanceDefaults.appearance()
    val afterpaySdkDefaults: AfterpayWidgetAppearance = AfterpayAppearanceDefaults.appearance()
    val payPalSdkDefaults: PayPalWidgetAppearance = PayPalAppearanceDefaults.appearance()
    val payPalVaultSdkDefaults: PayPalPaymentSourceWidgetAppearance =
        PayPalPaymentSourceAppearanceDefaults.appearance()
    val googlePaySdkDefaults: GooglePayWidgetAppearance = GooglePayAppearanceDefaults.appearance()
    val colesPaySdkDefaults: ColesPayWidgetAppearance =
        ColesPayWidgetAppearanceDefaults.appearance()
    // For 3DS, ensure ThreeDSAppearanceDefaults.appearance() is also @Composable if its internals depend on theme
    val integrated3DSSdkDefaults: ThreeDSWidgetAppearance = ThreeDSAppearanceDefaults.appearance()
    val standalone3DSSdkDefaults: ThreeDSWidgetAppearance = ThreeDSAppearanceDefaults.appearance()

    // Effect to update ViewModel when theme changes
    // On first init: Apply full defaults
    // On theme change: Update theme-dependent properties while preserving user customizations
    LaunchedEffect(isActuallyDark) {
        val isInitialized = stylingViewModel.addressWidgetAppearance.value != null
        
        // Update all widget appearances with theme-appropriate defaults
        // If already initialized, preserve user customizations
        stylingViewModel.updateInitialAddressDefaults(addressSdkDefaults, preserveCustomizations = isInitialized)
        stylingViewModel.updateInitialCardDetailsDefaults(cardSdkDefaults, preserveCustomizations = isInitialized)
        stylingViewModel.updateInitialGiftCardDetailsDefaults(giftCardSdkDefaults, preserveCustomizations = isInitialized)
        stylingViewModel.updateInitialPayPalVaultDefaults(payPalVaultSdkDefaults, preserveCustomizations = isInitialized)
        
        // Widgets without customization tracking - always update (these typically only have theme colors)
        stylingViewModel.updateInitialClickToPayDefaults(clickToPaySdkDefaults)
        stylingViewModel.updateInitialAfterpayDefaults(afterpaySdkDefaults)
        stylingViewModel.updateInitialPayPalDefaults(payPalSdkDefaults)
        stylingViewModel.updateInitialGooglePayDefaults(googlePaySdkDefaults)
        stylingViewModel.updateInitialColesPayDefaults(colesPaySdkDefaults)
        stylingViewModel.updateInitialIntegrated3DSDefaults(integrated3DSSdkDefaults)
        stylingViewModel.updateInitialStandalone3DSDefaults(standalone3DSSdkDefaults)
    }

    // Observe a combined state or a specific one to gate the UI
    val isAddressInitialized by stylingViewModel.addressWidgetAppearance.map { it != null }
        .collectAsState(initial = false)
    val isCardInitialized by stylingViewModel.cardDetailsWidgetAppearance.map { it != null }
        .collectAsState(initial = false)
    val isGiftCardInitialized by stylingViewModel.giftCardWidgetAppearance.map { it != null }
        .collectAsState(initial = false)
    val isClickToPayInitialized by stylingViewModel.clickToPayWidgetAppearance.map { it != null }
        .collectAsState(initial = false)
    val isAfterpayInitialized by stylingViewModel.afterpayWidgetAppearance.map { it != null }
        .collectAsState(initial = false)
    val isPayPalInitialized by stylingViewModel.paypalWidgetAppearance.map { it != null }
        .collectAsState(initial = false)
    val isPayPalVaultInitialized by stylingViewModel.paypalVaultWidgetAppearance.map { it != null }
        .collectAsState(initial = false)
    val isGooglePayWidgetAppearanceInitialized by stylingViewModel.googlePayWidgetAppearance.map { it != null }
        .collectAsState(initial = false)
    val isColesPayWidgetAppearanceInitialized by stylingViewModel.colesPayWidgetAppearance.map { it != null }
        .collectAsState(initial = false)
    val isIntegrated3DSWidgetAppearanceInitialized by stylingViewModel.integrated3DSWidgetAppearance.map { it != null }
        .collectAsState(initial = false)
    val isStandalone3DSWidgetAppearanceInitialized by stylingViewModel.standalone3DSWidgetAppearance.map { it != null }
        .collectAsState(initial = false)

    Scaffold(
        topBar = {
            // Route-aware toolbar content
            CenterAppTopBar(
                title = actionBarDetails.title,
                showTitle = actionBarDetails.showTitle,
                actionContent = if (actionBarDetails.route == "shop") {
                    {
                        val cartManager = remember { CartManager.shared }
                        val cartItemCount by cartManager.cartItems.collectAsState()
                        val itemCount = cartItemCount.sumOf { it.quantity }

                        var pulse by remember { mutableStateOf(false) }
                        val badgeScale by animateFloatAsState(
                            targetValue = if (pulse) 1.2f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "toolbarBadgeScale"
                        )
                        var lastCount by remember { mutableIntStateOf(itemCount) }
                        val haptic = LocalHapticFeedback.current
                        LaunchedEffect(itemCount) {
                            if (itemCount > lastCount) {
                                pulse = true
                                delay(300)
                                pulse = false
                            }
                            lastCount = itemCount
                        }

                        // Cart button with badge (uses default ripple)
                        BadgedBox(
                            badge = {
                                if (itemCount > 0) {
                                    Badge(modifier = Modifier.scale(badgeScale)) {
                                        val countText =
                                            if (itemCount > 99) "99+" else itemCount.toString()
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = countText,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                navController.navigate("cart")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Shopping Cart ($itemCount items)"
                                )
                            }
                        }

                        // Account/Profile button
                        IconButton(onClick = { navController.navigate("account") }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Account Profile"
                            )
                        }
                    }
                } else null,
                // For checkout route, default back arrow via onBackButtonClick
                navigationContent = null,
                onActionButtonClick = { navController.navigate("account") },
                onBackButtonClick = if (actionBarDetails.showBackButton) {
                    {
                        val currentRoute = actionBarDetails.route
                        if (currentRoute.contains("checkout")) {
                            // Let checkout handle back navigation between steps first
                            val handled =
                                com.paydock.sample.feature.checkout.presentation.CheckoutBackCoordinator.onToolbarBack?.invoke() == true
                            if (!handled) {
                                navController.popBackStack()
                            }
                        } else {
                            navController.navigateUp()
                        }
                    }
                } else null
            )
        },
        bottomBar = {
            if (!actionBarDetails.showBackButton) {
                BottomNavigation(navController = navController)
            }
        },
        content = { innerPadding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPadding)
                    // This caters for keyboard changes within compose
                    .consumeWindowInsets(paddingValues = innerPadding)
                    .imePadding()
            ) {
                // Always render NavigationGraph to ensure activity result launchers are registered
                // Show loading overlay when appearances are still initializing
                Box(modifier = Modifier.fillMaxSize()) {
                    NavigationGraph(
                        navController = navController,
                        stylingViewModel,
                        onThemeSelected
                    )

                    if (!(isAddressInitialized && isCardInitialized && isGiftCardInitialized && isClickToPayInitialized && isAfterpayInitialized && isPayPalInitialized && isPayPalVaultInitialized && isGooglePayWidgetAppearanceInitialized && isColesPayWidgetAppearanceInitialized && isIntegrated3DSWidgetAppearanceInitialized && isStandalone3DSWidgetAppearanceInitialized)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    )
}

// Helper class to hold action bar details
class ActionBarDetails(
    val title: String,
    val showBackButton: Boolean,
    val showTitle: Boolean,
    val route: String,
)

// Function to calculate action bar details, remember the result
@Composable
fun rememberActionBarDetails(navController: NavHostController, context: Context): ActionBarDetails {
    var actionBarTitle by rememberSaveable { mutableStateOf("") }
    var showBackButton by rememberSaveable { mutableStateOf(false) }
    var showTitle by rememberSaveable { mutableStateOf(true) }
    var route by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            actionBarTitle = backStackEntry.getRouteTitle(context)
            showBackButton = backStackEntry.showBackButton()
            showTitle = backStackEntry.showTitle()
            route = backStackEntry.destination.route ?: ""
        }
    }

    return remember(actionBarTitle, showBackButton, showTitle, route) {
        ActionBarDetails(actionBarTitle, showBackButton, showTitle, route)
    }
}

@PreviewLightDark
@Composable
internal fun PreviewMainScreen() {
    SampleTheme {
        MainScreenView(isActuallyDark = isSystemInDarkTheme()) {}
    }
}