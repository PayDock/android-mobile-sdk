/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
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

package com.paydock.core.data.injection

import android.content.Context
import com.paydock.BuildConfig
import com.paydock.core.data.injection.modules.sdkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.Koin
import org.koin.core.context.GlobalContext.startKoin

/**
 * A helper class for managing the Koin application context associated with the Mobile SDK.
 *
 * @property appContext The Android application context used for initializing the Koin application.
 */
internal class MobileSDKKoinContext(private val appContext: Context) {

    /**
     * The Koin instance associated with the Mobile SDK.
     * This property will be initialized as soon as the class is constructed.
     */
    var koin: Koin
        private set

    init {
        // Start Koin here
        koin = startKoin {
            androidContext(appContext)
            if (BuildConfig.DEBUG) androidLogger()
            modules(sdkModule)
        }.koin
    }

    companion object {
        @Volatile
        private var instance: MobileSDKKoinContext? = null

        /**
         * Initializes the MobileSDKKoinContext with the given application context.
         * This should be called once when the application starts.
         *
         * @param context The application context.
         */
        @Synchronized
        fun init(context: Context) {
            if (instance == null) {
                instance = MobileSDKKoinContext(context)
            }
        }
    }
}