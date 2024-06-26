package com.paydock.core.utils.decoder.injection

import com.paydock.core.utils.decoder.StringDecoder
import com.paydock.core.utils.decoder.UriDecoder
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Injection module responsible for handling String decoder.
 */
val stringDecoderKoinModule = module {
    singleOf(::UriDecoder) { bind<StringDecoder>() }
}