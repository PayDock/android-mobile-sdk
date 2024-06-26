package com.paydock.core.data.injection

import com.paydock.MobileSDK
import org.koin.core.Koin
import org.koin.core.component.KoinComponent

/**
 * Interface for components that require access to the Koin instance associated with the Mobile SDK.
 * Implementing this interface allows classes to access Koin functionalities for dependency injection.
 */
interface MobileSDKKoinComponent : KoinComponent {

    /**
     * Overrides the [getKoin] function from the [KoinComponent] interface to provide access to the Koin instance
     * associated with the Mobile SDK.
     *
     * @return The Koin instance associated with the Mobile SDK.
     * @throws IllegalStateException if the MobileSDK has not been initialized.
     */
    override fun getKoin(): Koin {
        // Ensure the MobileSDK has been initialized
        val koinContext = MobileSDK.getInstance().koinContext
            ?: error(IllegalStateException("MobileSDK has not been initialized. Call MobileSDK.initialize() first."))
        return koinContext.koin
    }
}