package com.paydock.feature.address.injection

import android.location.Geocoder
import com.paydock.core.data.injection.modules.dispatchersModule
import com.paydock.core.utils.decoder.injection.stringDecoderKoinModule
import com.paydock.feature.address.presentation.viewmodels.AddressDetailsViewModel
import com.paydock.feature.address.presentation.viewmodels.AddressSearchViewModel
import com.paydock.feature.address.presentation.viewmodels.CountryAutoCompleteViewModel
import com.paydock.feature.address.presentation.viewmodels.ManualAddressViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module
import java.util.Locale

/**
 * Koin module for Address-related components including repositories, use cases, and view models.
 **/
val addressDetailsModule = module {
    includes(dispatchersModule, stringDecoderKoinModule)

    single { Geocoder(get(), Locale.getDefault()) }
    viewModelOf(::AddressSearchViewModel)
    viewModelOf(::ManualAddressViewModel)
    viewModelOf(::CountryAutoCompleteViewModel)
    viewModelOf(::AddressDetailsViewModel)
}
