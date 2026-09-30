package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.paydock.sample.feature.style.StylingViewModel
import com.paydock.sample.feature.threeDS.presentation.Standalone3DSDemoViewModel
import com.paydock.sample.feature.widgets.ui.components.standalone3ds.Standalone3DSDemo

@Suppress("UnusedParameter") // Keeps the signature shared with the other widget items
@Composable
fun StandaloneThreeDSItem(
    context: Context,
    stylingViewModel: StylingViewModel,
    viewModel: Standalone3DSDemoViewModel = hiltViewModel(),
) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    Standalone3DSDemo(
        viewModel = viewModel,
        onDone = { backDispatcher?.onBackPressed() }
    )
}
