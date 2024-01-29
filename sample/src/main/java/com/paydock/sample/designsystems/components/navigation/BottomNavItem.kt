/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:58 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.sample.designsystems.components.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.paydock.sample.R

sealed class BottomNavItem(
    @StringRes val label: Int,
    @DrawableRes val icon_outlined: Int,
    @DrawableRes val icon_filled: Int,
    val route: String
) {
    object Checkout : BottomNavItem(
        R.string.nav_checkout,
        R.drawable.ic_checkout,
        R.drawable.ic_checkout_selected,
        "checkout"
    )

    object Widgets :
        BottomNavItem(
            R.string.nav_widgets,
            R.drawable.ic_widgets,
            R.drawable.ic_widgets_selected,
            "widgets"
        )

    object Style :
        BottomNavItem(
            R.string.nav_style,
            R.drawable.ic_style,
            R.drawable.ic_style_selected,
            "style"
        )

    object Settings : BottomNavItem(
        R.string.nav_settings,
        R.drawable.ic_settings,
        R.drawable.ic_settings_selected,
        "settings"
    )
}