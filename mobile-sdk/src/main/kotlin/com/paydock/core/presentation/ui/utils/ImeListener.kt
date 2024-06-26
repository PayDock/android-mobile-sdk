package com.paydock.core.presentation.ui.utils

import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Remembers and provides the current IME (Input Method Editor) state.
 *
 * @return A [State] containing the current IME state (true if the keyboard is open, false otherwise).
 */
@Composable
fun rememberImeState(): State<Boolean> {
    // Create a mutable state to store the current IME state
    val imeState = remember {
        mutableStateOf(false)
    }

    // Access the current view in the composition
    val view = LocalView.current

    // Set up a disposable effect to observe changes in the global layout
    DisposableEffect(view) {
        // Define a listener for changes in global layout
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            // Check if the keyboard is open by querying the visibility of the IME window insets
            val isKeyboardOpen = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) ?: true

            // Update the IME state
            imeState.value = isKeyboardOpen
        }

        // Add the listener to the view's viewTreeObserver
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)

        // Remove the listener when the composable is disposed
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    // Return the current IME state
    return imeState
}