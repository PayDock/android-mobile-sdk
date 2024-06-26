package com.paydock.sample.feature.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.paydock.sample.designsystems.components.CenterAppTopBar
import com.paydock.sample.designsystems.components.navigation.BottomNavigation
import com.paydock.sample.designsystems.components.navigation.NavigationGraph
import com.paydock.sample.designsystems.components.navigation.getRouteTitle
import com.paydock.sample.designsystems.components.navigation.showBackButton
import com.paydock.sample.designsystems.components.navigation.showTitle
import com.paydock.sample.designsystems.theme.SampleTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // We handle all the insets manually
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            SampleTheme {
                MainScreenView()
            }
        }
    }
}

@Composable
fun MainScreenView() {
    val context = LocalContext.current
    val navController = rememberNavController()
    var actionBarTitle by rememberSaveable { mutableStateOf("") }
    var showBackButton by rememberSaveable { mutableStateOf(false) }
    var showTitle by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            // You can map the title based on the route using:
            actionBarTitle = backStackEntry.getRouteTitle(context)
            showBackButton = backStackEntry.showBackButton()
            showTitle = backStackEntry.showTitle()
        }
    }

    Scaffold(
        topBar = {
            CenterAppTopBar(
                title = actionBarTitle,
                showTitle = showTitle,
                onBackButtonClick = if (showBackButton) {
                    { navController.popBackStack() }
                } else null
            )
        },
        bottomBar = {
            if (!showBackButton) {
                BottomNavigation(navController = navController)
            }
        },
        content = { innerPadding ->
            // padding calculated by scaffold
            // it doesn't have to be a column,
            // can be another component that accepts a modifier with padding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(paddingValues = innerPadding)
            ) {
                // all content should be here
                NavigationGraph(navController = navController)
            }
        }
    )
}

@Preview
@Composable
private fun PreviewMainScreen() {
    SampleTheme {
        MainScreenView()
    }
}