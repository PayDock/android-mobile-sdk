package com.paydock.sample.feature.wallet.injection

import com.paydock.sample.core.data.injection.DispatchersModule
import com.paydock.sample.core.data.injection.NetworkModule
import com.paydock.sample.feature.wallet.data.api.WalletApi
import com.paydock.sample.feature.wallet.data.repository.WalletRepositoryImpl
import com.paydock.sample.feature.wallet.domain.repository.WalletRepository
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
class WalletModule {

    @Singleton
    @Provides
    fun provideWalletApi(retrofit: Retrofit): WalletApi {
        return retrofit.create(WalletApi::class.java)
    }

    @Singleton
    @Provides
    fun provideWalletRepository(
        @Named("IO") dispatcher: CoroutineDispatcher,
        walletApi: WalletApi
    ): WalletRepository {
        return WalletRepositoryImpl(dispatcher, walletApi)
    }
}