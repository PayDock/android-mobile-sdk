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
        MobileSDK.Builder()
            .environment(Environment.SANDBOX)
            // Set flag for non-production builds
            .enableTestMode(false)
            // Uncomment if wanting to use custom SDK theme
            // .applyTheme(theme)
            .build(this)
    }
}
