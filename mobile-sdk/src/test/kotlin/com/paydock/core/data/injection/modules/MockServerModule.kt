package com.paydock.core.data.injection.modules

import okhttp3.mockwebserver.MockWebServer
import org.koin.dsl.module

val mockServerModule = module {
    single { MockWebServer() }
}