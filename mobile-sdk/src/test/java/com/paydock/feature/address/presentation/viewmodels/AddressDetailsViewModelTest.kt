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

package com.paydock.feature.address.presentation.viewmodels

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.address.domain.model.BillingAddress
import com.paydock.feature.address.presentation.state.AddressDetailsViewState
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AddressDetailsViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: AddressDetailsViewModel

    @Before
    fun setup() {
        viewModel = AddressDetailsViewModel(dispatchersProvider)
    }

    @Test
    fun `updateDefaultAddress should set address details view state`() = runTest {
        val savedAddress = BillingAddress(
            addressLine1 = "1 Park Avenue",
            city = "Manchester",
            state = "Greater Manchester",
            postalCode = "M11 5MW",
            country = "United Kingdom"
        )
        // ACTION
        viewModel.updateDefaultAddress(savedAddress)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertEquals(savedAddress.addressLine1, state.addressLine1)
        assertEquals(savedAddress.addressLine2 ?: "", state.addressLine2)
        assertEquals(savedAddress.city, state.city)
        assertEquals(savedAddress.state, state.state)
        assertEquals(savedAddress.postalCode, state.postalCode)
        assertEquals(savedAddress.country, state.country)
    }

    @Test
    fun `updateManualAddress should update address details view state`() = runTest {
        val addressDetailsViewState = AddressDetailsViewState(
            addressLine1 = "1 Park Avenue",
            city = "Manchester",
            state = "Greater Manchester",
            postalCode = "M11 5MW",
            country = "United Kingdom"
        )
        // ACTION
        viewModel.updateManualAddress(addressDetailsViewState)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertEquals(addressDetailsViewState.addressLine1, state.addressLine1)
        assertEquals(addressDetailsViewState.addressLine2, state.addressLine2)
        assertEquals(addressDetailsViewState.city, state.city)
        assertEquals(addressDetailsViewState.state, state.state)
        assertEquals(addressDetailsViewState.postalCode, state.postalCode)
        assertEquals(addressDetailsViewState.country, state.country)
    }
}
