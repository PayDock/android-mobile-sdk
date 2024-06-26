package com.paydock.feature.card.injection

import com.paydock.MobileSDK
import com.paydock.core.data.injection.modules.dataModule
import com.paydock.core.data.injection.modules.dispatchersModule
import com.paydock.core.data.injection.modules.provideHttpClient
import com.paydock.core.data.injection.modules.provideHttpEngine
import com.paydock.core.domain.injection.domainModule
import com.paydock.core.utils.decoder.injection.stringDecoderKoinModule
import com.paydock.feature.card.data.api.auth.CardAuthInterceptor
import com.paydock.feature.card.data.repository.CardDetailsRepositoryImpl
import com.paydock.feature.card.domain.repository.CardDetailsRepository
import com.paydock.feature.card.domain.usecase.TokeniseCreditCardFlowUseCase
import com.paydock.feature.card.domain.usecase.TokeniseCreditCardUseCase
import com.paydock.feature.card.domain.usecase.TokeniseGiftCardUseCase
import com.paydock.feature.card.presentation.viewmodels.CardDetailsViewModel
import com.paydock.feature.card.presentation.viewmodels.GiftCardViewModel
import okhttp3.Interceptor
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for Card-related components including repositories, use cases, and view models.
 **/
val cardDetailsModule = module {
    // Include other necessary modules
    includes(dispatchersModule, dataModule, domainModule, stringDecoderKoinModule)

    // Provide a CardAuthInterceptor with the public key from the MobileSDK
    single<Interceptor>(named("CardAuth")) {
        CardAuthInterceptor(publicKey = MobileSDK.getInstance().publicKey)
    }

    // Provide an HTTP engine for card operations with custom interceptors
    single(named("CardEngine")) {
        provideHttpEngine(get(), get(named("CardAuth")), get(), get())
    }

    // Provide an HTTP client for card operations using the provided engine
    single(named("CardClient")) {
        provideHttpClient(get(), get(named("CardEngine")))
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

    // Factory methods for creating instances of ViewModels
    viewModelOf(::CardDetailsViewModel)
    viewModelOf(::GiftCardViewModel)
}