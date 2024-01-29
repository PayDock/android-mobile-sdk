/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:58 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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