package com.paydock.feature.address.injection

import android.location.Geocoder
import com.paydock.core.utils.decoder.injection.stringDecoderKoinModule
import com.paydock.feature.address.presentation.viewmodels.AddressDetailsViewModel
import com.paydock.feature.address.presentation.viewmodels.AddressSearchViewModel
import com.paydock.feature.address.presentation.viewmodels.CountryAutoCompleteViewModel
import com.paydock.feature.address.presentation.viewmodels.ManualAddressViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.util.Locale

/**
 * Koin module for Address-related components including repositories, use cases, and view models.
 **/
internal val addressDetailsModule = module {
    includes(stringDecoderKoinModule)

    single { Geocoder(get(), Locale.getDefault()) }
    viewModelOf(::AddressSearchViewModel)
    viewModelOf(::ManualAddressViewModel)
    viewModelOf(::CountryAutoCompleteViewModel)
    viewModelOf(::AddressDetailsViewModel)
}
