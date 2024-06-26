package com.paydock.core.util

import com.paydock.core.data.util.DispatchersProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
object TestDispatchersProviderImpl : DispatchersProvider {
    override fun getMain(): CoroutineDispatcher = UnconfinedTestDispatcher()
    override fun getMainImmediate(): CoroutineDispatcher = UnconfinedTestDispatcher()
    override fun getIO(): CoroutineDispatcher = UnconfinedTestDispatcher()
    override fun getDefault(): CoroutineDispatcher = UnconfinedTestDispatcher()
}
