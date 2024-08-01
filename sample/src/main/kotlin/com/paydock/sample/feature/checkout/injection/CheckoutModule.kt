package com.paydock.sample.feature.checkout.injection

import com.paydock.sample.core.data.injection.DispatchersModule
import com.paydock.sample.core.data.injection.NetworkModule
import com.paydock.sample.feature.checkout.data.api.CheckoutApi
import com.paydock.sample.feature.checkout.data.repository.CheckoutRepositoryImpl
import com.paydock.sample.feature.checkout.domain.repository.CheckoutRepository
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
class CheckoutModule {

    @Singleton
    @Provides
    fun provideCheckoutApi(retrofit: Retrofit): CheckoutApi {
        return retrofit.create(CheckoutApi::class.java)
    }

    @Singleton
    @Provides
    fun provideCheckoutRepository(
        @Named("IO") dispatcher: CoroutineDispatcher,
        checkoutApi: CheckoutApi
    ): CheckoutRepository {
        return CheckoutRepositoryImpl(dispatcher, checkoutApi)
    }
}