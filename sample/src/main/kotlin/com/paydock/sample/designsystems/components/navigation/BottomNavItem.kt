package com.paydock.sample.designsystems.components.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.paydock.sample.R

sealed class BottomNavItem(
    @param:StringRes val label: Int,
    @param:DrawableRes val iconOutlined: Int,
    @param:DrawableRes val iconFilled: Int,
    val route: String,
) {
    data object Shop : BottomNavItem(
        R.string.nav_shop,
        R.drawable.ic_checkout,
        R.drawable.ic_checkout_selected,
        "shop"
    )

    data object Widgets :
        BottomNavItem(
            R.string.nav_widgets,
            R.drawable.ic_widgets,
            R.drawable.ic_widgets_selected,
            "widgets"
        )

    data object Style :
        BottomNavItem(
            R.string.nav_style,
            R.drawable.ic_style,
            R.drawable.ic_style_selected,
            "style"
        )

    data object Config :
        BottomNavItem(
            R.string.nav_config,
            R.drawable.ic_config,
            R.drawable.ic_config_selected,
            "config"
        )
}