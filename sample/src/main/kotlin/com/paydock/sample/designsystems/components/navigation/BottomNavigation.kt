package com.paydock.sample.designsystems.components.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun BottomNavigation(navController: NavController) {
    val items = listOf(
        BottomNavItem.Shop,
        BottomNavItem.Widgets,
        BottomNavItem.Style
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination
        items.forEach { item ->
            val selected = currentRoute?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = if (selected) item.iconFilled else item.iconOutlined),
                        contentDescription = stringResource(id = item.label)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = item.label),
                        textAlign = TextAlign.Center
                    )
                },
                alwaysShowLabel = true,
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { screenRoute ->
                            popUpTo(screenRoute) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
@PreviewLightDark
internal fun PreviewBottomNavigation() {
    SampleTheme {
        val navController = rememberNavController()
        BottomNavigation(navController)
    }
}