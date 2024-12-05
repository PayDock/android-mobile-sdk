package com.paydock.sample.feature.vaults.injection

import com.paydock.sample.core.data.injection.DispatchersModule
import com.paydock.sample.core.data.injection.NetworkModule
import com.paydock.sample.feature.vaults.data.api.VaultsApi
import com.paydock.sample.feature.vaults.data.repository.VaultsRepositoryImpl
import com.paydock.sample.feature.vaults.domain.repository.VaultsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [NetworkModule::class, DispatchersModule::class])
@InstallIn(SingletonComponent::class)
class VaultsModule {

    @Singleton
    @Provides
    fun provideVaultsApi(retrofit: Retrofit): VaultsApi {
        return retrofit.create(VaultsApi::class.java)
    }

    @Singleton
    @Provides
    fun provideVaultsRepository(
        @Named("IO") dispatcher: CoroutineDispatcher,
        vaults: VaultsApi,
    ): VaultsRepository {
        return VaultsRepositoryImpl(dispatcher, vaults)
    }
}