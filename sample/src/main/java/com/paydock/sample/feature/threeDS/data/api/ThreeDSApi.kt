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

package com.paydock.sample.feature.threeDS.data.api

import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.threeDS.data.api.dto.CreateIntegratedThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.data.api.dto.ThreeDSTokenResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ThreeDSApi {

    @POST("/v1/charges/3ds")
    suspend fun createIntegrated3dsToken(
        @Header("x-user-public-key") publicKey: String = BuildConfig.PUBLIC_KEY,
        @Body request: CreateIntegratedThreeDSTokenRequest
    ): ThreeDSTokenResponse

    @POST("/v1/charges/standalone-3ds")
    suspend fun createStandalone3dsToken(
        @Header("x-user-secret-key") secretKey: String = BuildConfig.SECRET_KEY,
        @Body request: CreateStandaloneThreeDSTokenRequest
    ): ThreeDSTokenResponse

}