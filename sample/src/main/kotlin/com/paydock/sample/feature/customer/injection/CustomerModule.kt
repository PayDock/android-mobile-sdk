package com.paydock.sample.feature.customer.injection

import com.paydock.sample.core.data.injection.DispatchersModule
import com.paydock.sample.core.data.injection.NetworkModule
import com.paydock.sample.feature.customer.data.api.CustomerApi
import com.paydock.sample.feature.customer.data.repository.CustomerRepositoryImpl
import com.paydock.sample.feature.customer.domain.repository.CustomerRepository
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
class CustomerModule {

    @Singleton
    @Provides
    fun provideCustomerApi(retrofit: Retrofit): CustomerApi {
        return retrofit.create(CustomerApi::class.java)
    }

    @Singleton
    @Provides
    fun provideCustomerRepository(
        @Named("IO") dispatcher: CoroutineDispatcher,
        customerApi: CustomerApi,
    ): CustomerRepository {
        return CustomerRepositoryImpl(dispatcher, customerApi)
    }
}