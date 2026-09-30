package com.paydock.feature.threeDS.standalone.presentation.utils

import com.paydock.core.presentation.util.WidgetLoadingDelegate
import org.junit.Test
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class Standalone3DSLoadingControllerTest {

    @Test
    fun `controller starts in loading state`() {
        assertTrue(Standalone3DSLoadingController().isLoading)
    }

    @Test
    fun `start notifies delegate`() {
        val delegate = mock(WidgetLoadingDelegate::class.java)
        val controller = Standalone3DSLoadingController()

        controller.start(delegate)

        assertTrue(controller.isLoading)
        inOrder(delegate).verify(delegate).widgetLoadingDidStart()
        verifyNoMoreInteractions(delegate)
    }

    @Test
    fun `update only notifies delegate on transitions`() {
        val delegate = mock(WidgetLoadingDelegate::class.java)
        val controller = Standalone3DSLoadingController()
        controller.start(delegate)

        controller.update(true, delegate) // no-op, already loading
        controller.update(false, delegate)
        controller.update(false, delegate) // no-op, already finished
        controller.update(true, delegate)
        controller.update(false, delegate)

        assertFalse(controller.isLoading)
        val order = inOrder(delegate)
        order.verify(delegate).widgetLoadingDidStart()
        order.verify(delegate).widgetLoadingDidFinish()
        order.verify(delegate).widgetLoadingDidStart()
        order.verify(delegate).widgetLoadingDidFinish()
        verifyNoMoreInteractions(delegate)
    }

    @Test
    fun `update without delegate only toggles state`() {
        val controller = Standalone3DSLoadingController()

        controller.update(false, null)
        assertFalse(controller.isLoading)

        controller.update(true, null)
        assertTrue(controller.isLoading)
    }

    @Test
    fun `update to the current state does not notify delegate`() {
        val delegate = mock(WidgetLoadingDelegate::class.java)
        val controller = Standalone3DSLoadingController()

        controller.update(true, delegate)

        verifyNoInteractions(delegate)
    }
}
