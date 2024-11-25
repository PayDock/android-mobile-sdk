package com.paydock.sample.designsystems.components.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.designsystems.theme.Theme

@Composable
fun BottomNavigation(navController: NavController) {
    val items = listOf(
        BottomNavItem.Checkout,
        BottomNavItem.Widgets,
        BottomNavItem.Style,
        BottomNavItem.Settings,
    )

    NavigationBar(
        containerColor = Theme.colors.primaryContainer,
        contentColor = Theme.colors.onPrimaryContainer,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination
        items.forEach { item ->
            val selected = currentRoute?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = if (selected) item.iconFilled else item.iconOutlined),
                        contentDescription = stringResource(id = item.label)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = item.label),
                        style = Theme.typography.caption,
                        fontWeight = FontWeight(500),
                        textAlign = TextAlign.Center,
                        color = Theme.colors.onPrimaryContainer
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Theme.colors.primary,
                    selectedIconColor = Theme.colors.onPrimaryContainer,
                    selectedTextColor = Theme.colors.onPrimaryContainer,
                    unselectedTextColor = Theme.colors.onPrimaryContainer,
                    unselectedIconColor = Theme.colors.onPrimaryContainer
                ),
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
@Preview
private fun PreviewBottomNavigation() {
    SampleTheme {
        val navController = rememberNavController()
        BottomNavigation(navController)
    }
}