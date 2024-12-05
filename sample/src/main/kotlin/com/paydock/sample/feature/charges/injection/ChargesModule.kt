package com.paydock.sample.feature.charges.injection

import com.paydock.sample.core.data.injection.DispatchersModule
import com.paydock.sample.core.data.injection.NetworkModule
import com.paydock.sample.feature.charges.data.api.ChargesApi
import com.paydock.sample.feature.charges.data.repository.ChargesRepositoryImpl
import com.paydock.sample.feature.charges.domain.repository.ChargesRepository
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
class ChargesModule {

    @Singleton
    @Provides
    fun provideChargesApi(retrofit: Retrofit): ChargesApi {
        return retrofit.create(ChargesApi::class.java)
    }

    @Singleton
    @Provides
    fun provideChargesRepository(
        @Named("IO") dispatcher: CoroutineDispatcher,
        chargesApi: ChargesApi,
    ): ChargesRepository {
        return ChargesRepositoryImpl(dispatcher, chargesApi)
    }
}