package com.paydock.core.presentation.ui.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * To control the system UI in your composables. ie. setSystemBarsColor(), setStatusBarColor() and setNavigationBarColor()
 */
@Composable
fun SystemUISideEffect(block: (SystemUiController) -> Unit) {
    val controller = rememberSystemUiController()
    SideEffect { block.invoke(controller) }
}
