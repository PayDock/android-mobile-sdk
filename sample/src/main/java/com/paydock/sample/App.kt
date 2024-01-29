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

package com.paydock.sample

import android.app.Application
import androidx.compose.ui.graphics.Color
import com.paydock.MobileSDK
import com.paydock.MobileSDKTheme
import com.paydock.core.domain.model.Environment
import com.paydock.sample.designsystems.theme.typography.AcidGroteskNormal
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // If we were to change the default SDK theme to our own custom theme
        val theme = MobileSDKTheme(
            colours = MobileSDKTheme.Colours.themeColours(
                light = MobileSDKTheme.Colours.lightThemeColors(
                    primary = Color.Blue,
                    onPrimary = Color.White,
                    text = Color.Black,
                    placeholder = Color.Gray,
                    success = Color.Green,
                    error = Color.Red,
                    background = Color.White,
                    outline = Color.DarkGray,
                ),
                dark = MobileSDKTheme.Colours.darkThemeColors(
                    primary = Color.Blue,
                    onPrimary = Color.White,
                    text = Color.White,
                    placeholder = Color.LightGray,
                    success = Color.Green,
                    error = Color.Red,
                    background = Color.Black,
                    outline = Color.Gray,
                ),
            ),
            dimensions = MobileSDKTheme.Dimensions.themeDimensions(
                cornerRadius = 4,
                shadow = 0,
                borderWidth = 1,
                spacing = 10
            ),
            font = MobileSDKTheme.FontName.themeFont(
                fonts = listOf(AcidGroteskNormal)
            )
        )
        MobileSDK
            .Builder(BuildConfig.PUBLIC_KEY)
            .environment(Environment.SANDBOX)
            // Uncomment if wanting to use custom SDK theme
            // .applyTheme(theme)
            .build(this)
    }
}
