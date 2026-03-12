package com.paydock.feature.zip.injection

import com.paydock.feature.zip.data.repository.ZipRepositoryImpl
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.feature.zip.domain.repository.ZipRepository
import com.paydock.feature.zip.domain.usecase.CreateZipPaymentSourceTokenUseCase
import com.paydock.feature.zip.domain.usecase.InitializeZipCheckoutUseCase
import com.paydock.feature.zip.presentation.viewmodels.ZipViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for Zip-related components including repositories, use cases, and view models.
 */
internal val zipModule = module {

    // Provide the repository for Zip operations
    single<ZipRepository> {
        ZipRepositoryImpl(
            dispatcher = get(named("IO")),
            client = get()
        )
    }

    // Use Cases
    factoryOf(::InitializeZipCheckoutUseCase)
    factoryOf(::CreateZipPaymentSourceTokenUseCase)

    // ViewModel
    viewModel { (config: ZipWidgetConfig) ->
        ZipViewModel(
            config = config,
            initializeZipCheckoutUseCase = get(),
            createZipPaymentSourceTokenUseCase = get(),
            dispatchers = get(),
            savedStateHandle = get()
        )
    }
}
