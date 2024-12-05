package com.paydock.feature.address.presentation.viewmodels

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.feature.address.presentation.state.AddressDetailsInputState
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
internal class AddressDetailsViewModelTest : BaseKoinUnitTest() {

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
        val addressDetailsInputState = AddressDetailsInputState(
            addressLine1 = "1 Park Avenue",
            city = "Manchester",
            state = "Greater Manchester",
            postalCode = "M11 5MW",
            country = "United Kingdom"
        )
        // ACTION
        viewModel.updateManualAddress(addressDetailsInputState)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertEquals(addressDetailsInputState.addressLine1, state.addressLine1)
        assertEquals(addressDetailsInputState.addressLine2, state.addressLine2)
        assertEquals(addressDetailsInputState.city, state.city)
        assertEquals(addressDetailsInputState.state, state.state)
        assertEquals(addressDetailsInputState.postalCode, state.postalCode)
        assertEquals(addressDetailsInputState.country, state.country)
    }
}
