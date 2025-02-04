package com.paydock.core.utils.reader.injection

import com.paydock.core.utils.reader.LocalFileReader
import com.paydock.core.utils.reader.LocalFileReaderImpl
import org.koin.dsl.module

/**
 * Injection module responsible for handling local file reading (assets & resources).
 */
val fileReaderKoinModule = module {
    single<LocalFileReader> { LocalFileReaderImpl(context = get()) }
}