package com.paydock.sample.feature.main

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
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

            var currentAppTheme by remember { mutableStateOf(AppTheme.SYSTEM) }

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
    val addressSdkDefaults: AddressDetailsWidgetAppearance = AddressDetailsAppearanceDefaults.appearance()
    val cardSdkDefaults: CardDetailsWidgetAppearance = CardDetailsAppearanceDefaults.appearance()
    val giftCardSdkDefaults: GiftCardWidgetAppearance = GiftCardAppearanceDefaults.appearance()
    val clickToPaySdkDefaults: ClickToPayWidgetAppearance = ClickToPayAppearanceDefaults.appearance()
    val afterpaySdkDefaults: AfterpayWidgetAppearance = AfterpayAppearanceDefaults.appearance()
    val payPalSdkDefaults: PayPalWidgetAppearance = PayPalAppearanceDefaults.appearance()
    val payPalVaultSdkDefaults: PayPalPaymentSourceWidgetAppearance = PayPalPaymentSourceAppearanceDefaults.appearance()
    val googlePaySdkDefaults: GooglePayWidgetAppearance = GooglePayAppearanceDefaults.appearance()
    val colesPaySdkDefaults: ColesPayWidgetAppearance = ColesPayWidgetAppearanceDefaults.appearance()
    // For 3DS, ensure ThreeDSAppearanceDefaults.appearance() is also @Composable if its internals depend on theme
    val integrated3DSSdkDefaults: ThreeDSWidgetAppearance = ThreeDSAppearanceDefaults.appearance()
    val standalone3DSSdkDefaults: ThreeDSWidgetAppearance = ThreeDSAppearanceDefaults.appearance()


    // Effect to update ViewModel when the theme (isActuallyDark) or any of the defaults change
    // The defaults themselves will change instance if isActuallyDark causes them to re-evaluate with different theme values
    LaunchedEffect(
        isActuallyDark, // Primary key for theme change
        addressSdkDefaults, cardSdkDefaults, giftCardSdkDefaults, clickToPaySdkDefaults,
        afterpaySdkDefaults, payPalSdkDefaults, payPalVaultSdkDefaults, googlePaySdkDefaults,
        colesPaySdkDefaults, integrated3DSSdkDefaults, standalone3DSSdkDefaults
    ) {
        // Now use the results of the @Composable calls
        stylingViewModel.updateInitialAddressDefaults(addressSdkDefaults)
        stylingViewModel.updateInitialCardDetailsDefaults(cardSdkDefaults)
        stylingViewModel.updateInitialGiftCardDetailsDefaults(giftCardSdkDefaults)
        stylingViewModel.updateInitialClickToPayDefaults(clickToPaySdkDefaults)
        stylingViewModel.updateInitialAfterpayDefaults(afterpaySdkDefaults)
        stylingViewModel.updateInitialPayPalDefaults(payPalSdkDefaults)
        stylingViewModel.updateInitialPayPalVaultDefaults(payPalVaultSdkDefaults)
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
            CenterAppTopBar(
                title = actionBarDetails.title,
                showTitle = actionBarDetails.showTitle,
                onActionButtonClick = { navController.navigate("account") },
                onBackButtonClick = if (actionBarDetails.showBackButton) {
                    { navController.navigateUp() }
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
                if (isAddressInitialized && isCardInitialized && isGiftCardInitialized && isClickToPayInitialized && isAfterpayInitialized && isPayPalInitialized && isPayPalVaultInitialized && isGooglePayWidgetAppearanceInitialized && isColesPayWidgetAppearanceInitialized && isIntegrated3DSWidgetAppearanceInitialized && isStandalone3DSWidgetAppearanceInitialized) {
                    NavigationGraph(
                        navController = navController,
                        stylingViewModel,
                        onThemeSelected
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
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
)

// Function to calculate action bar details, remember the result
@Composable
fun rememberActionBarDetails(navController: NavHostController, context: Context): ActionBarDetails {
    var actionBarTitle by rememberSaveable { mutableStateOf("") }
    var showBackButton by rememberSaveable { mutableStateOf(false) }
    var showTitle by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            actionBarTitle = backStackEntry.getRouteTitle(context)
            showBackButton = backStackEntry.showBackButton()
            showTitle = backStackEntry.showTitle()
        }
    }

    return remember(actionBarTitle, showBackButton, showTitle) {
        ActionBarDetails(actionBarTitle, showBackButton, showTitle)
    }
}

@PreviewLightDark
@Composable
internal fun PreviewMainScreen() {
    SampleTheme {
        MainScreenView(isActuallyDark = isSystemInDarkTheme()) {}
    }
}