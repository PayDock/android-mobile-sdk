package com.paydock.sample.core.injection

import com.paydock.sample.core.presentation.utils.AccessTokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideAccessTokenProvider(): AccessTokenProvider = AccessTokenProvider()
}