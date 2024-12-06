package com.paydock.core.presentation.util

/**
 * Interface for managing UI loading states within a widget.
 *
 * This interface defines methods to handle the start and finish of a loading process,
 * typically used to show or hide loading indicators during asynchronous operations.
 */
interface WidgetLoadingDelegate {

    /**
     * Called when a widget's loading process starts.
     *
     * Implementations of this method should handle the initiation of any UI elements
     * that indicate loading, such as showing a progress bar or a loading spinner.
     */
    fun widgetLoadingDidStart()

    /**
     * Called when a widget's loading process finishes.
     *
     * Implementations of this method should handle the termination of any UI elements
     * indicating loading, such as hiding a progress bar or a loading spinner.
     */
    fun widgetLoadingDidFinish()
}