package com.paydock.core.utils

import com.paydock.core.data.util.DispatchersProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher

object TestDispatchersProviderImpl : DispatchersProvider {
    override fun getMain(): CoroutineDispatcher = StandardTestDispatcher()
    override fun getMainImmediate(): CoroutineDispatcher = StandardTestDispatcher()
    override fun getIO(): CoroutineDispatcher = StandardTestDispatcher()
    override fun getDefault(): CoroutineDispatcher = StandardTestDispatcher()
}
