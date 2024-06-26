package com.paydock.designsystems.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class SdkDropDownMenuTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSdkDropDownMenu() {
        // Define the test items and other required variables
        val items = listOf("Item 1", "Item 2", "Item 3")
        var selectedItem: String? = null

        // Launch the composable with the test parameters
        composeTestRule.setContent {
            SdkDropDownMenu(
                expanded = true,
                itemWidth = 200.dp,
                items = items,
                onItemSelected = {
                    selectedItem = it
                },
                onDismissed = { }
            )
        }

        // Verify that the dropdown menu and items are displayed
        composeTestRule.onNodeWithTag("sdkDropDownMenu").assertIsDisplayed()
        items.forEach { item ->
            composeTestRule.onNodeWithText(item).assertIsDisplayed()
        }

        // Click on the first item
        composeTestRule.onNodeWithText("Item 1").performClick()

        // Verify that the selected item is updated
        assertEquals("Item 1", selectedItem)
    }
}