package com.paydock.sample.feature.tokens.injection

import com.paydock.sample.core.data.injection.DispatchersModule
import com.paydock.sample.core.data.injection.NetworkModule
import com.paydock.sample.feature.tokens.data.api.TokensApi
import com.paydock.sample.feature.tokens.data.repository.TokensRepositoryImpl
import com.paydock.sample.feature.tokens.domain.repository.TokensRepository
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
class TokensModule {

    @Singleton
    @Provides
    fun provideTokensApi(retrofit: Retrofit): TokensApi {
        return retrofit.create(TokensApi::class.java)
    }

    @Singleton
    @Provides
    fun provideTokensRepository(
        @Named("IO") dispatcher: CoroutineDispatcher,
        tokensApi: TokensApi,
    ): TokensRepository {
        return TokensRepositoryImpl(dispatcher, tokensApi)
    }
}