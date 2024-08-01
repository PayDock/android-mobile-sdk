package com.paydock.sample.designsystems.components.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.list.ListScreen
import com.paydock.sample.feature.checkout.models.CheckoutType
import com.paydock.sample.feature.checkout.ui.CheckoutStandalone
import com.paydock.sample.feature.checkout.ui.CheckoutWorkflow
import com.paydock.sample.feature.settings.ui.SettingsScreen
import com.paydock.sample.feature.style.ui.StyleScreen
import com.paydock.sample.feature.widgets.ui.WidgetInfoScreen
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = BottomNavItem.Checkout.route) {
        composable(BottomNavItem.Checkout.route) {
            ListScreen(items = CheckoutType.entries.sortedBy { it.displayName() }) { checkoutType ->
                navController.currentBackStackEntry?.arguments?.putString(
                    "widget_type",
                    checkoutType.name
                )
                navController.navigate("checkout_info/${checkoutType.name}")
            }
        }
        composable(BottomNavItem.Widgets.route) {
            ListScreen(items = WidgetType.entries.sortedBy { it.displayName() }) { widgetType ->
                navController.currentBackStackEntry?.arguments?.putString(
                    "widget_type",
                    widgetType.name
                )
                navController.navigate("widget_info/${widgetType.name}")
            }
        }
        composable(BottomNavItem.Style.route) {
            StyleScreen()
        }
        composable(BottomNavItem.Settings.route) {
            SettingsScreen()
        }

        composable(
            "checkout_info/{checkout_type}",
            arguments = listOf(navArgument("checkout_type") { type = NavType.StringType })
        ) { navBackStackEntry ->
            /* Extracting the id from the route */
            navBackStackEntry.arguments?.getString("checkout_type")?.let { type ->
                val checkoutType = CheckoutType.valueOf(type)
                when (checkoutType) {
                    CheckoutType.STANDALONE -> CheckoutStandalone()
                    CheckoutType.PAYMENT_WORKFLOW -> CheckoutWorkflow()
                }
            }
        }

        composable(
            "widget_info/{widget_type}",
            arguments = listOf(navArgument("widget_type") { type = NavType.StringType })
        ) { navBackStackEntry ->
            /* Extracting the id from the route */
            navBackStackEntry.arguments?.getString("widget_type")?.let { type ->
                val widgetType = WidgetType.valueOf(type)
                WidgetInfoScreen(widgetType)
            }
        }
    }
}

fun NavBackStackEntry.getRouteTitle(context: Context): String {
    return when (destination.route) {
        "checkout" -> context.getString(R.string.nav_checkout)
        "widgets" -> context.getString(R.string.nav_widgets)
        "style" -> context.getString(R.string.nav_style)
        "settings" -> context.getString(R.string.nav_settings)
        "widget_info/{widget_type}" -> {
            arguments?.getString("widget_type")?.let { type ->
                val widgetType = WidgetType.valueOf(type)
                widgetType.displayName()
            } ?: ""
        }

        "checkout_info/{checkout_type}" -> {
            arguments?.getString("checkout_type")?.let { type ->
                val checkoutType = CheckoutType.valueOf(type)
                checkoutType.displayName()
            } ?: ""
        }

        else -> ""
    }
}

fun NavBackStackEntry.showBackButton(): Boolean {
    return when (destination.route) {
        BottomNavItem.Checkout.route,
        BottomNavItem.Widgets.route,
        BottomNavItem.Style.route,
        BottomNavItem.Settings.route -> false

        else -> true
    }
}

fun NavBackStackEntry.showTitle(): Boolean {
    return when (destination.route) {
        "checkout_info/{checkout_type}" -> false
        else -> true
    }
}