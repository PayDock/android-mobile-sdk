package com.paydock.sample

import android.app.Application
import androidx.compose.ui.graphics.Color
import com.paydock.MobileSDK
import com.paydock.MobileSDKTheme
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
                textFieldCornerRadius = 4,
                buttonCornerRadius = 4,
                borderWidth = 1
            ),
            font = MobileSDKTheme.FontName.themeFont(
                fonts = listOf(AcidGroteskNormal)
            )
        )
        MobileSDK.Builder()
            .environment(BuildConfig.SDK_ENVIRONMENT)
            // Set flag for non-production builds
            .enableTestMode(BuildConfig.ENABLE_TEST_MODE)
            // Uncomment if wanting to use custom SDK theme
            // .applyTheme(theme)
            .build(this)
    }
}
