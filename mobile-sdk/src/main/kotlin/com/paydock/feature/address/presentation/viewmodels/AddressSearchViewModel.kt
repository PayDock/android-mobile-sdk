package com.paydock.feature.address.presentation.viewmodels

import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.os.Build
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.telemetry.SdkWidget
import com.paydock.core.telemetry.reportSdkErrorWhileActive
import com.paydock.designsystems.components.search.SearchViewModel
import com.paydock.telemetry.Telemetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * ViewModel for handling the address search functionality.
 *
 * @param geocoder The Geocoder instance for performing address searches.
 * @param dispatchers Provides coroutines dispatchers for background tasks.
 * @param telemetry The telemetry facade the view model reports its handled errors to.
 */
internal class AddressSearchViewModel(
    private val geocoder: Geocoder,
    dispatchers: DispatchersProvider,
    private val telemetry: Telemetry,
) : SearchViewModel<Address>(dispatchers) {

    /**
     * Whether a search failure was reported: a search runs per keystroke, so only the first failure is reported per
     * view model (AD2).
     */
    private val searchFailureReported = AtomicBoolean(false)

    override fun stringResults(): List<String> =
        searchResult.value.mapNotNull { it.getAddressLine(0) }

    /**
     * Performs an address search and returns the search results as a Flow.
     *
     * @param query The search query.
     * @return A Flow emitting the search results as a Result.
     */
    @Suppress("DEPRECATION", "TooGenericExceptionCaught", "SwallowedException")
    override fun searchItems(query: String): Flow<List<Address>> = flow {
        if (query.isNotBlank()) {
            try {
                // Use suspendCoroutine to bridge between callback-based and coroutine-based APIs
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCoroutine { continuation ->
                        // Call the geocoder with a custom GeocodeListener
                        geocoder.getFromLocationName(
                            query,
                            MobileSDKConstants.AddressConfig.MAX_SEARCH_RESULTS,
                            object : GeocodeListener {
                                override fun onGeocode(addresses: MutableList<Address>) {
                                    // Resume the continuation with the search results
                                    continuation.resume(addresses.toList())
                                }

                                override fun onError(errorMessage: String?) {
                                    // Resume the continuation with an empty list on error
                                    continuation.resume(emptyList())
                                }
                            }
                        )
                    }
                } else {
                    // Call the geocoder directly for older Android versions
                    geocoder.getFromLocationName(
                        query,
                        MobileSDKConstants.AddressConfig.MAX_SEARCH_RESULTS
                    )?.toList()
                        ?: emptyList()
                }
                // Emit a successful result with the addresses list
                emit(addresses)
            } catch (e: Exception) {
                if (searchFailureReported.compareAndSet(false, true)) {
                    telemetry.reportSdkErrorWhileActive(e, SdkWidget.ADDRESS_DETAILS)
                }
                // Emit empty result on exception
                emit(emptyList())
            }
        } else {
            // Emit a success result with an empty list for blank queries
            emit(emptyList())
        }
    }
}