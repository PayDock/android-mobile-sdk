package com.paydock.feature.card.injection

import com.paydock.core.utils.decoder.injection.stringDecoderKoinModule
import com.paydock.core.utils.reader.injection.fileReaderKoinModule
import com.paydock.feature.card.data.repository.CardRepositoryImpl
import com.paydock.feature.card.domain.model.integration.GiftCardWidgetConfig
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.repository.CardRepository
import com.paydock.feature.card.domain.usecase.CreateCardPaymentTokenFlowUseCase
import com.paydock.feature.card.domain.usecase.CreateCardPaymentTokenUseCase
import com.paydock.feature.card.domain.usecase.CreateGiftCardPaymentTokenUseCase
import com.paydock.feature.card.domain.usecase.GetCardSchemasUseCase
import com.paydock.feature.card.presentation.viewmodels.CardDetailsViewModel
import com.paydock.feature.card.presentation.viewmodels.GiftCardViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for Card-related components including repositories, use cases, and view models.
 **/
internal val cardDetailsModule = module {
    // Include other necessary modules
    includes(stringDecoderKoinModule, fileReaderKoinModule)

    // Factory methods for creating instances of ViewModels with access tokens
    viewModel { (accessToken: String, gatewayId: String?, schemeConfig: SupportedSchemeConfig) ->
        CardDetailsViewModel(accessToken, gatewayId, schemeConfig, get(), get(), get())
    }
    viewModel { (config: GiftCardWidgetConfig) ->
        GiftCardViewModel(config, get(), get())
    }

    // Provide the repository for managing tokens
    single<CardRepository> {
        CardRepositoryImpl(dispatcher = get(named("IO")), client = get(), jsonReader = get())
    }

    // Token Based UseCases
    factoryOf(::CreateCardPaymentTokenUseCase)
    factoryOf(::CreateCardPaymentTokenFlowUseCase)
    factoryOf(::CreateGiftCardPaymentTokenUseCase)

    // Bin-Management Based UseCases
    factoryOf(::GetCardSchemasUseCase)
}