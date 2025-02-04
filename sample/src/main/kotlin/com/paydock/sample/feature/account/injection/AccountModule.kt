package com.paydock.sample.feature.account.injection

import com.paydock.sample.core.data.injection.DispatchersModule
import com.paydock.sample.core.data.injection.NetworkModule
import com.paydock.sample.feature.account.data.api.AccountApi
import com.paydock.sample.feature.account.data.repository.AccountRepositoryImpl
import com.paydock.sample.feature.account.domain.repository.AccountRepository
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
class AccountModule {

    @Singleton
    @Provides
    fun provideAccountApi(retrofit: Retrofit): AccountApi {
        return retrofit.create(AccountApi::class.java)
    }

    @Singleton
    @Provides
    fun provideAccountRepository(
        @Named("IO") dispatcher: CoroutineDispatcher,
        accountApi: AccountApi,
    ): AccountRepository {
        return AccountRepositoryImpl(dispatcher, accountApi)
    }
}