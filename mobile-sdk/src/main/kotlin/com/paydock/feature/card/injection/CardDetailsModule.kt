package com.paydock.feature.card.injection

import com.paydock.BuildConfig
import com.paydock.MobileSDK
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.injection.modules.dataModule
import com.paydock.core.data.injection.modules.dispatchersModule
import com.paydock.core.domain.injection.domainModule
import com.paydock.core.network.NetworkClientBuilder
import com.paydock.core.utils.decoder.injection.stringDecoderKoinModule
import com.paydock.feature.card.data.repository.CardDetailsRepositoryImpl
import com.paydock.feature.card.domain.repository.CardDetailsRepository
import com.paydock.feature.card.domain.usecase.TokeniseCreditCardFlowUseCase
import com.paydock.feature.card.domain.usecase.TokeniseCreditCardUseCase
import com.paydock.feature.card.domain.usecase.TokeniseGiftCardUseCase
import com.paydock.feature.card.presentation.viewmodels.CardDetailsViewModel
import com.paydock.feature.card.presentation.viewmodels.GiftCardViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for Card-related components including repositories, use cases, and view models.
 **/
val cardDetailsModule = module {
    // Include other necessary modules
    includes(dispatchersModule, dataModule, domainModule, stringDecoderKoinModule)

    // Provide an HTTP client for card operations using the provided engine
    single(named("CardClient")) {
        NetworkClientBuilder.create()
            .setBaseUrl(MobileSDK.getInstance().baseUrl)
            .setSslPins(listOf(MobileSDKConstants.Network.SSH_HASH))
            .setDebug(BuildConfig.DEBUG)
            .build()
    }

    // Provide the repository for managing card details
    single<CardDetailsRepository> {
        CardDetailsRepositoryImpl(dispatcher = get(named("IO")), client = get(named("CardClient")))
    }

    // Factory methods for creating instances of UseCases
    factoryOf(::TokeniseCreditCardUseCase)
    factoryOf(::TokeniseCreditCardFlowUseCase)
    factoryOf(::TokeniseGiftCardUseCase)
    factoryOf(::TokeniseGiftCardUseCase)

    // Factory methods for creating instances of ViewModels with access tokens
    viewModel { (accessToken: String) ->
        CardDetailsViewModel(accessToken, get(), get())
    }
    viewModel { (accessToken: String) ->
        GiftCardViewModel(accessToken, get(), get())
    }
}