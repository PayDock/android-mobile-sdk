/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 4:15 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
