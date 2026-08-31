package com.paydock.designsystems.core

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit coverage for [WidgetDefaults.shouldUseColumnLayout], the pure layout-adaptation decision that
 * drives whether the expiry / security-code fields sit side-by-side (row) or stack (column).
 *
 * This is the deterministic core behind the widget reflowing on width/orientation changes and at
 * large font scales (Dynamic Type). Threshold with the widget's params (minFieldWidth 150.dp,
 * fieldWeight 0.5) is an effective width of 300.dp.
 */
class WidgetDefaultsTest {

    private val minFieldWidth = 150.dp
    private val fieldWeight = 0.5f

    @Test
    fun `wide layout keeps expiry and security side by side`() {
        assertFalse(
            WidgetDefaults.shouldUseColumnLayout(
                availableWidth = 640.dp,
                fontScale = 1f,
                minFieldWidth = minFieldWidth,
                fieldWeight = fieldWeight
            )
        )
    }

    @Test
    fun `narrow layout stacks expiry and security in a column`() {
        assertTrue(
            WidgetDefaults.shouldUseColumnLayout(
                availableWidth = 280.dp,
                fontScale = 1f,
                minFieldWidth = minFieldWidth,
                fieldWeight = fieldWeight
            )
        )
    }

    @Test
    fun `large font scale forces a column even at a moderate width`() {
        // 360.dp / 2.0 = 180.dp effective, below the 300.dp row threshold.
        assertTrue(
            WidgetDefaults.shouldUseColumnLayout(
                availableWidth = 360.dp,
                fontScale = 2f,
                minFieldWidth = minFieldWidth,
                fieldWeight = fieldWeight
            )
        )
    }

    @Test
    fun `moderate width at default font stays in a row`() {
        // 360.dp / 1.0 = 360.dp effective, at or above the 300.dp threshold.
        assertFalse(
            WidgetDefaults.shouldUseColumnLayout(
                availableWidth = 360.dp,
                fontScale = 1f,
                minFieldWidth = minFieldWidth,
                fieldWeight = fieldWeight
            )
        )
    }
}
