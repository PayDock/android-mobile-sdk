package com.paydock.sample.feature.card.injection

import com.paydock.sample.core.data.injection.DispatchersModule
import com.paydock.sample.core.data.injection.NetworkModule
import com.paydock.sample.feature.card.data.api.CardApi
import com.paydock.sample.feature.card.data.repository.CardRepositoryImpl
import com.paydock.sample.feature.card.domain.repository.CardRepository
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
class CardModule {

    @Singleton
    @Provides
    fun provideCardApi(retrofit: Retrofit): CardApi {
        return retrofit.create(CardApi::class.java)
    }

    @Singleton
    @Provides
    fun provideCardRepository(
        @Named("IO") dispatcher: CoroutineDispatcher,
        cardApi: CardApi,
    ): CardRepository {
        return CardRepositoryImpl(dispatcher, cardApi)
    }
}