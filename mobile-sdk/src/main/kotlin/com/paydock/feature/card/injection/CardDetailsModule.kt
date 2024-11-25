package com.paydock.feature.card.injection

import com.paydock.core.utils.decoder.injection.stringDecoderKoinModule
import com.paydock.feature.card.presentation.viewmodels.CardDetailsViewModel
import com.paydock.feature.card.presentation.viewmodels.GiftCardViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for Card-related components including repositories, use cases, and view models.
 **/
internal val cardDetailsModule = module {
    // Include other necessary modules
    includes(stringDecoderKoinModule)

    // Factory methods for creating instances of ViewModels with access tokens
    viewModel { (accessToken: String, gatewayId: String?) ->
        CardDetailsViewModel(accessToken, gatewayId, get(), get())
    }
    viewModel { (accessToken: String) ->
        GiftCardViewModel(accessToken, get(), get())
    }
}