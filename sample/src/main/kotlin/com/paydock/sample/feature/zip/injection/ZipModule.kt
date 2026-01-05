package com.paydock.sample.feature.zip.injection

import com.paydock.sample.core.data.injection.DispatchersModule
import com.paydock.sample.core.data.injection.NetworkModule
import com.paydock.sample.feature.zip.data.api.ZipApi
import com.paydock.sample.feature.zip.data.repository.ZipRepositoryImpl
import com.paydock.sample.feature.zip.domain.repository.ZipRepository
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
class ZipModule {

    @Singleton
    @Provides
    fun provideZipApi(retrofit: Retrofit): ZipApi {
        return retrofit.create(ZipApi::class.java)
    }

    @Singleton
    @Provides
    fun provideZipRepository(
        @Named("IO") dispatcher: CoroutineDispatcher,
        zipApi: ZipApi,
    ): ZipRepository {
        return ZipRepositoryImpl(dispatcher, zipApi)
    }
}

