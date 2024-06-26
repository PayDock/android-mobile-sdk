package com.paydock.sample.core.data.injection

import com.paydock.MobileSDK
import com.paydock.core.domain.model.Environment
import com.paydock.sample.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    @Provides
    fun provideBaseUrl(): String {
        return when (MobileSDK.getInstance().environment) {
            Environment.STAGING -> "https://apista.paydock.com"
            Environment.SANDBOX -> "https://api-sandbox.paydock.com"
            Environment.PRODUCTION -> "https://api.paydock.com"
        }
    }


    @Singleton
    @Provides
    fun provideRetrofit(baseUrl: String, okHttp: OkHttpClient): Retrofit {
        return Retrofit.Builder().apply {
            addConverterFactory(GsonConverterFactory.create())
            client(okHttp)
            baseUrl(baseUrl)
        }.build()
    }

    @Singleton
    @Provides
    fun provideOkHttp(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            connectTimeout(1, TimeUnit.MINUTES)
            readTimeout(1, TimeUnit.MINUTES)
            writeTimeout(5, TimeUnit.MINUTES)
            retryOnConnectionFailure(true)
            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                addInterceptor(logging)
            }
        }.build()
    }
}