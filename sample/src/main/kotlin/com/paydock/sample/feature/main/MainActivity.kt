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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.paydock.feature.threeDS.integrated.presentation.ui.MPGSThreeDSWidgetAppearance
import com.paydock.feature.threeDS.integrated.presentation.ui.MPGSThreeDSWidgetAppearanceDefaults
import com.paydock.feature.threeDS.standalone.presentation.ui.StandaloneThreeDSWidgetAppearance
import com.paydock.feature.threeDS.standalone.presentation.ui.StandaloneThreeDSWidgetAppearanceDefaults
import com.paydock.feature.zip.presentation.ZipWidgetAppearance
import com.paydock.feature.zip.presentation.ZipWidgetAppearanceDefaults
import com.paydock.sample.designsystems.components.CenterAppTopBar
import com.paydock.sample.designsystems.components.navigation.BottomNavigation
import com.paydock.sample.designsystems.components.navigation.NavigationGraph
import com.paydock.sample.designsystems.components.navigation.getRouteTitle
import com.paydock.sample.designsystems.components.navigation.showBackButton
import com.paydock.sample.designsystems.components.navigation.showTitle
import com.paydock.sample.designsystems.theme.AppTheme
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.shop.data.CartManager
import com.paydock.sample.feature.style.StylingViewModel
import dagger.hilt.android.AndroidEntryPoint
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
                stateSaver = Saver(
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
    configViewModel: ConfigViewModel = hiltViewModel(),
    isActuallyDark: Boolean,
    onThemeSelected: (AppTheme) -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val actionBarDetails = rememberActionBarDetails(navController, context)

    // Track font scale to respond to accessibility settings changes
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale

    // *** Call @Composable defaults here, in the Composable scope ***
    // These will re-evaluate if MainScreenView recomposes due to isActuallyDark or fontScale changing
    // Call composable functions first, then remember the result
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
    val mpgs3dsSdkDefaults: MPGSThreeDSWidgetAppearance = MPGSThreeDSWidgetAppearanceDefaults.appearance()
    val standalone3DSSdkDefaults: StandaloneThreeDSWidgetAppearance =
        StandaloneThreeDSWidgetAppearanceDefaults.appearance()
    val zipSdkDefaults: ZipWidgetAppearance = ZipWidgetAppearanceDefaults.appearance()

    // Effect to update ViewModel when theme or font scale changes
    // On first init: Apply full defaults
    // On theme/font scale change: Update theme-dependent properties while preserving user customizations
    LaunchedEffect(isActuallyDark, fontScale) {
        val isInitialized = stylingViewModel.addressWidgetAppearance.value != null

        // Update all widget appearances with theme-appropriate defaults
        // If already initialized, preserve user customizations
        stylingViewModel.updateInitialAddressDefaults(
            addressSdkDefaults,
            preserveCustomizations = isInitialized
        )
        stylingViewModel.updateInitialCardDetailsDefaults(
            cardSdkDefaults,
            preserveCustomizations = isInitialized
        )
        stylingViewModel.updateInitialGiftCardDetailsDefaults(
            giftCardSdkDefaults,
            preserveCustomizations = isInitialized
        )
        stylingViewModel.updateInitialPayPalVaultDefaults(
            payPalVaultSdkDefaults,
            preserveCustomizations = isInitialized
        )

        // Widgets without customization tracking - always update (these typically only have theme colors)
        stylingViewModel.updateInitialClickToPayDefaults(clickToPaySdkDefaults)
        stylingViewModel.updateInitialAfterpayDefaults(afterpaySdkDefaults)
        stylingViewModel.updateInitialPayPalDefaults(payPalSdkDefaults)
        stylingViewModel.updateInitialGooglePayDefaults(googlePaySdkDefaults)
        stylingViewModel.updateInitialColesPayDefaults(colesPaySdkDefaults)
        stylingViewModel.updateInitialMPGS3dsDefaults(mpgs3dsSdkDefaults)
        stylingViewModel.updateInitialStandalone3DSDefaults(standalone3DSSdkDefaults)
        stylingViewModel.updateInitialZipDefaults(zipSdkDefaults)

        // Config defaults are now initialized in ConfigViewModel constructor
        // No need to call updateInitial* methods here unless overriding defaults
    }

    // Optimize: Collect the state directly and use derivedStateOf for initialization checks
    val addressWidgetAppearance by stylingViewModel.addressWidgetAppearance.collectAsState()
    val cardDetailsWidgetAppearance by stylingViewModel.cardDetailsWidgetAppearance.collectAsState()
    val giftCardWidgetAppearance by stylingViewModel.giftCardWidgetAppearance.collectAsState()
    val clickToPayWidgetAppearance by stylingViewModel.clickToPayWidgetAppearance.collectAsState()
    val afterpayWidgetAppearance by stylingViewModel.afterpayWidgetAppearance.collectAsState()
    val paypalWidgetAppearance by stylingViewModel.paypalWidgetAppearance.collectAsState()
    val paypalVaultWidgetAppearance by stylingViewModel.paypalVaultWidgetAppearance.collectAsState()
    val googlePayWidgetAppearance by stylingViewModel.googlePayWidgetAppearance.collectAsState()
    val colesPayWidgetAppearance by stylingViewModel.colesPayWidgetAppearance.collectAsState()
    val mpgs3dsWidgetAppearance by stylingViewModel.mpgs3dsWidgetAppearance.collectAsState()
    val standalone3DSWidgetAppearance by stylingViewModel.standalone3DSWidgetAppearance.collectAsState()
    val zipWidgetAppearance by stylingViewModel.zipWidgetAppearance.collectAsState()

    // Combine all states using derivedStateOf to avoid recomposition when individual states don't change
    val isAllInitialized by remember {
        derivedStateOf {
            addressWidgetAppearance != null &&
                    cardDetailsWidgetAppearance != null &&
                    giftCardWidgetAppearance != null &&
                    clickToPayWidgetAppearance != null &&
                    afterpayWidgetAppearance != null &&
                    paypalWidgetAppearance != null &&
                    paypalVaultWidgetAppearance != null &&
                    googlePayWidgetAppearance != null &&
                    colesPayWidgetAppearance != null &&
                    mpgs3dsWidgetAppearance != null &&
                    standalone3DSWidgetAppearance != null &&
                    zipWidgetAppearance != null
        }
    }

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
                        val haptic = LocalHapticFeedback.current

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
                        configViewModel,
                        onThemeSelected
                    )

                    if (!isAllInitialized) {
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Use derivedStateOf to compute values only when route changes, reducing recompositions
    // derivedStateOf automatically tracks navBackStackEntry changes
    val actionBarDetails by remember {
        derivedStateOf {
            val backStackEntry = navBackStackEntry
            ActionBarDetails(
                title = backStackEntry?.getRouteTitle(context) ?: "",
                showBackButton = backStackEntry?.showBackButton() ?: false,
                showTitle = backStackEntry?.showTitle() ?: true,
                route = backStackEntry?.destination?.route ?: ""
            )
        }
    }

    return actionBarDetails
}

@PreviewLightDark
@Composable
internal fun PreviewMainScreen() {
    SampleTheme {
        MainScreenView(isActuallyDark = isSystemInDarkTheme()) {}
    }
}