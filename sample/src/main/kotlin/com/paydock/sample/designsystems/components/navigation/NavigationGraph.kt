package com.paydock.sample.designsystems.components.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.AppTheme
import com.paydock.sample.feature.account.ui.AccountScreen
import com.paydock.sample.feature.checkout.domain.model.CheckoutStep
import com.paydock.sample.feature.checkout.ui.EnhancedCheckoutScreen
import com.paydock.sample.feature.checkout.ui.OrderConfirmationScreen
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.mapper.mapWidgetTypeToConfigComponents
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.config.ui.CartConfigScreen
import com.paydock.sample.feature.config.ui.CheckoutConfigScreen
import com.paydock.sample.feature.config.ui.ConfigComponentListScreen
import com.paydock.sample.feature.config.ui.ConfigPropertiesScreen
import com.paydock.sample.feature.config.ui.ConfigScreen
import com.paydock.sample.feature.config.ui.ConfigSubComponentListScreen
import com.paydock.sample.feature.config.ui.ConfigWidgetListScreen
import com.paydock.sample.feature.config.ui.GlobalConfigScreen
import com.paydock.sample.feature.config.ui.PaymentProcessorConfigScreen
import com.paydock.sample.feature.config.ui.properties.zip.ZipShippingAddressHeader
import com.paydock.sample.feature.shop.data.CartManager
import com.paydock.sample.feature.shop.ui.CartScreen
import com.paydock.sample.feature.shop.ui.ProductListScreen
import com.paydock.sample.feature.style.StylingViewModel
import com.paydock.sample.feature.style.mapper.mapAppearanceComponentToSubComponents
import com.paydock.sample.feature.style.mapper.mapWidgetTypeToAppearanceComponents
import com.paydock.sample.feature.style.models.StyleAppearanceComponent
import com.paydock.sample.feature.style.ui.StyleComponentListScreen
import com.paydock.sample.feature.style.ui.StylePropertiesScreen
import com.paydock.sample.feature.style.ui.StyleSubComponentListScreen
import com.paydock.sample.feature.style.ui.StyleWidgetListScreen
import com.paydock.sample.feature.style.utils.FontHelper
import com.paydock.sample.feature.style.utils.LocalFontHelper
import com.paydock.sample.feature.widgets.ui.WidgetInfoScreen
import com.paydock.sample.feature.widgets.ui.WidgetsScreen
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun NavigationGraph(
    navController: NavHostController,
    stylingViewModel: StylingViewModel,
    configViewModel: ConfigViewModel,
    onThemeSelected: (AppTheme) -> Unit
) {
    NavHost(navController, startDestination = "shop") {
        composable("shop") {
            ProductListScreen()
        }
        composable("cart") {
            CartScreen(
                onCheckout = { navController.navigate("checkout?step=information") },
                onContinueShopping = { navController.navigateUp() }
            )
        }
        composable(
            route = "checkout?step={step}",
            arguments = listOf(navArgument("step") {
                type = NavType.StringType; defaultValue = "information"
            })
        ) { backStackEntry ->
            val stepArg = backStackEntry.arguments?.getString("step") ?: "information"
            val initialStep = if (stepArg.equals("payment", ignoreCase = true))
                CheckoutStep.PAYMENT
            else CheckoutStep.INFORMATION

            EnhancedCheckoutScreen(
                onDismiss = {
                    navController.navigate("shop") {
                        popUpTo("shop") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onOrderComplete = { isSuccess ->
                    navController.navigate("order_confirmation/$isSuccess") {
                        popUpTo("checkout") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                initialStep = initialStep
            )
        }
        composable(
            "order_confirmation/{isSuccess}",
            arguments = listOf(navArgument("isSuccess") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isSuccess = backStackEntry.arguments?.getBoolean("isSuccess") ?: false
            OrderConfirmationScreen(
                isSuccess = isSuccess,
                onContinueShopping = {
                    // Clear cart on successful order
                    if (isSuccess) {
                        CartManager.shared.clearCart()
                    }
                    navController.navigate("shop") {
                        popUpTo("shop") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRetryCheckout = {
                    // Navigate back to checkout at Payment step, keeping cart intact
                    navController.navigate("checkout?step=payment") {
                        popUpTo("order_confirmation/{isSuccess}") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCancel = {
                    // Return to shop, keeping cart intact
                    navController.navigate("shop") {
                        popUpTo("shop") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(BottomNavItem.Widgets.route) {
            WidgetsScreen { widgetType ->
                navController.currentBackStackEntry?.arguments?.putString(
                    "widgetType",
                    widgetType.name
                )
                navController.navigate("widget_info/${widgetType.name}")
            }
        }
        composable(BottomNavItem.Style.route) {
            StyleWidgetListScreen(onThemeSelected) { widgetType ->
                navController.currentBackStackEntry?.arguments?.putString(
                    "widgetType",
                    widgetType.name
                )
                navController.navigate("style_components/${widgetType.name}")
            }
        }
        composable(BottomNavItem.Config.route) {
            ConfigScreen(
                onGlobalConfigSelected = {
                    navController.navigate("global_config")
                },
                onCheckoutConfigSelected = {
                    navController.navigate("config_checkout")
                },
                onWidgetsConfigSelected = {
                    navController.navigate("config_widgets_list")
                }
            )
        }
        composable("global_config") {
            GlobalConfigScreen(configViewModel = configViewModel)
        }
        composable("config_checkout") {
            CheckoutConfigScreen(
                onPaymentProcessorSelected = {
                    navController.navigate("config_payment_processor")
                }
            )
        }
        composable("config_payment_processor") {
            PaymentProcessorConfigScreen(viewModel = configViewModel)
        }
        composable("config_cart") {
            CartConfigScreen(configViewModel = configViewModel)
        }
        composable("config_widgets_list") {
            ConfigWidgetListScreen(
                onWidgetSelected = { widgetType ->
                    navController.currentBackStackEntry?.arguments?.putString(
                        "widgetType",
                        widgetType.name
                    )
                    navController.navigate("config_components/${widgetType.name}")
                },
                onCartSelected = {
                    navController.navigate("config_cart")
                }
            )
        }
        composable(
            "widget_info/{widgetType}",
            arguments = listOf(navArgument("widgetType") { type = NavType.StringType })
        ) { navBackStackEntry ->
            /* Extracting the id from the route */
            navBackStackEntry.arguments?.getString("widgetType")?.let { type ->
                val widgetType = WidgetType.valueOf(type)
                WidgetInfoScreen(widgetType, stylingViewModel, configViewModel)
            }
        }
        composable(
            "style_components/{widgetType}",
            arguments = listOf(navArgument("widgetType") { type = NavType.StringType })
        ) { navBackStackEntry ->
            navBackStackEntry.arguments?.getString("widgetType")?.let { type ->
                val widgetType = WidgetType.valueOf(type)
                val styleItems = remember {
                    widgetType.mapWidgetTypeToAppearanceComponents().sortedBy { it.displayName() }
                }
                StyleComponentListScreen(styleItems) { selectedItem ->
                    if (selectedItem.hasSubComponents) {
                        navController.navigate("style_sub_components/${widgetType.name}/${selectedItem.name}") // Pass widgetType too if sub-components can vary by parent widget
                    } else {
                        // Navigate to the concrete styling properties screen
                        navController.navigate("style_properties/${widgetType.name}/${selectedItem.name}") // Pass widgetType if properties vary
                    }
                }
            }
        }
        composable(
            "style_sub_components/{widgetType}/{styleComponent}",
            arguments = listOf(
                navArgument("widgetType") { type = NavType.StringType },
                navArgument("styleComponent") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("widgetType")
            val component = backStackEntry.arguments?.getString("styleComponent")

            if (type != null && component != null) {
                val widgetType = WidgetType.valueOf(type)
                val styleComponent = StyleAppearanceComponent.valueOf(component)

                val items: List<StyleAppearanceComponent> =
                    remember {
                        styleComponent.mapAppearanceComponentToSubComponents()
                            ?.sortedBy { it.displayName() } ?: emptyList()
                    }

                StyleSubComponentListScreen(
                    subComponents = items,
                    onStyleComponentClicked = { selectedItem ->
                        if (selectedItem.hasSubComponents) {
                            navController.navigate("style_sub_components/${widgetType.name}/${selectedItem.name}") // Pass widgetType too if sub-components can vary by parent widget
                        } else {
                            // Navigate to the concrete styling properties screen
                            navController.navigate("style_properties/${widgetType.name}/${selectedItem.name}") // Pass widgetType if properties vary
                        }
                    }
                )
            }
        }

        composable(
            route = "style_properties/{widgetType}/{styleComponent}", // contextName could be widgetType or parentItemName
            arguments = listOf(
                navArgument("widgetType") { type = NavType.StringType },
                navArgument("styleComponent") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val widgetType = backStackEntry.arguments?.getString("widgetType")
            val styleComponent = backStackEntry.arguments?.getString("styleComponent")

            if (widgetType != null && styleComponent != null) {
                CompositionLocalProvider(LocalFontHelper provides FontHelper) {
                    // This is your final screen where actual ColorPickers, FontChoosers, etc., live
                    StylePropertiesScreen(
                        widgetContext = WidgetType.valueOf(widgetType), // To know what specific style you are editing (e.g. TITLE of ADDRESS_WIDGET)
                        styleItemName = StyleAppearanceComponent.valueOf(styleComponent),
                        stylingViewModel = stylingViewModel
                    )
                }
            }
        }

        composable(
            "config_components/{widgetType}",
            arguments = listOf(navArgument("widgetType") { type = NavType.StringType })
        ) { navBackStackEntry ->
            navBackStackEntry.arguments?.getString("widgetType")?.let { type ->
                val widgetType = WidgetType.valueOf(type)
                val configItems = remember {
                    widgetType.mapWidgetTypeToConfigComponents().sortedBy { it.displayName() }
                }
                ConfigComponentListScreen(configItems) { selectedItem ->
                    if (selectedItem.hasSubComponents) {
                        navController.navigate("config_sub_components/${widgetType.name}/${selectedItem.name}")
                    } else {
                        navController.navigate("config_properties/${widgetType.name}/${selectedItem.name}")
                    }
                }
            }
        }
        composable(
            "config_sub_components/{widgetType}/{configComponent}",
            arguments = listOf(
                navArgument("widgetType") { type = NavType.StringType },
                navArgument("configComponent") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("widgetType")
            val component = backStackEntry.arguments?.getString("configComponent")

            if (type != null && component != null) {
                val widgetType = WidgetType.valueOf(type)
                val configComponent = ConfigComponent.valueOf(component)

                val items: List<ConfigComponent> =
                    remember {
                        configComponent.mapConfigComponentToSubComponents()
                            ?.sortedBy { it.displayName() } ?: emptyList()
                    }

                val zipConfigHeader: (@Composable () -> Unit)? =
                    if (configComponent == ConfigComponent.ZIP_SHIPPING_ADDRESS && widgetType == WidgetType.ZIP) {
                        {
                            val config by configViewModel.zipWidgetConfig.collectAsState()
                            ZipShippingAddressHeader(
                                config = config,
                                widgetContext = widgetType,
                                configViewModel = configViewModel
                            )
                        }
                    } else null

                ConfigSubComponentListScreen(
                    subComponents = items,
                    onConfigComponentClicked = { selectedItem ->
                        if (selectedItem.hasSubComponents) {
                            navController.navigate("config_sub_components/${widgetType.name}/${selectedItem.name}")
                        } else {
                            navController.navigate("config_properties/${widgetType.name}/${selectedItem.name}")
                        }
                    },
                    header = zipConfigHeader
                )
            }
        }

        composable(
            route = "config_properties/{widgetType}/{configComponent}",
            arguments = listOf(
                navArgument("widgetType") { type = NavType.StringType },
                navArgument("configComponent") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val widgetType = backStackEntry.arguments?.getString("widgetType")
            val configComponent = backStackEntry.arguments?.getString("configComponent")

            if (widgetType != null && configComponent != null) {
                ConfigPropertiesScreen(
                    widgetContext = WidgetType.valueOf(widgetType),
                    configItemName = ConfigComponent.valueOf(configComponent),
                    configViewModel = configViewModel
                )
            }
        }

        composable("account") {
            AccountScreen()
        }
    }
}

fun NavBackStackEntry.getRouteTitle(context: Context): String {
    return when (destination.route) {
        "shop" -> context.getString(R.string.nav_shop)
        "cart" -> context.getString(R.string.title_cart)
        "checkout" -> context.getString(R.string.nav_checkout)
        "order_confirmation/{isSuccess}" -> "" // No title for confirmation screen
        "widgets" -> context.getString(R.string.nav_widgets)
        "style" -> context.getString(R.string.nav_style)
        "config" -> context.getString(R.string.nav_config)
        "account" -> context.getString(R.string.title_my_account)
        "widget_info/{widgetType}" -> {
            arguments?.getString("widgetType")?.let { type ->
                val widgetType = WidgetType.valueOf(type)
                widgetType.displayName()
            } ?: ""
        }

        "style_components/{widgetType}" -> {
            arguments?.getString("widgetType")?.let { type ->
                val widgetType = WidgetType.valueOf(type)
                widgetType.displayName()
            } ?: ""
        }

        "style_sub_components/{widgetType}/{styleComponent}",
        "style_properties/{widgetType}/{styleComponent}" -> {
            arguments?.getString("styleComponent")?.let { type ->
                val styleComponent = StyleAppearanceComponent.valueOf(type)
                styleComponent.displayName()
            } ?: ""
        }

        "config_sub_components/{widgetType}/{configComponent}",
        "config_properties/{widgetType}/{configComponent}" -> {
            arguments?.getString("configComponent")?.let { type ->
                val configComponent = ConfigComponent.valueOf(type)
                configComponent.displayName()
            } ?: ""
        }

        else -> ""
    }
}

fun NavBackStackEntry.showBackButton(): Boolean {
    return when (destination.route) {
        "shop",
        "order_confirmation/{isSuccess}", // No back button on confirmation screen
        BottomNavItem.Shop.route,
        BottomNavItem.Widgets.route,
        BottomNavItem.Style.route,
        BottomNavItem.Config.route -> false

        else -> true
    }
}

fun NavBackStackEntry.showTitle(): Boolean {
    return when (destination.route) {
        "shop" -> false
        "order_confirmation/{isSuccess}" -> false // No title for confirmation screen
        BottomNavItem.Shop.route -> false
        else -> true
    }
}
