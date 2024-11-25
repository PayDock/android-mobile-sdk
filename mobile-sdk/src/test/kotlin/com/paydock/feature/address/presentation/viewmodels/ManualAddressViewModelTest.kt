package com.paydock.feature.address.presentation.viewmodels

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.address.domain.model.integration.BillingAddress
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
internal class ManualAddressViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: ManualAddressViewModel

    @Before
    fun setup() {
        viewModel = ManualAddressViewModel(dispatchersProvider)
    }

    @Test
    fun `setSavedAddress should update address details state`() = runTest {
        val savedAddress = BillingAddress(
            addressLine1 = "1 Park Avenue",
            city = "Manchester",
            state = "Greater Manchester",
            postalCode = "M11 5MW",
            country = "United Kingdom"
        )
        // ACTION
        viewModel.setSavedAddress(savedAddress)
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
    fun `updateAddressLine1 should update addressLine1`() = runTest {
        val newName = "1 Park Avenue"
        // ACTION
        viewModel.updateAddressLine1(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.addressLine1)
    }

    @Test
    fun `updateAddressLine2 should update addressLine2`() = runTest {
        val newName = "2 Park Avenue"
        // ACTION
        viewModel.updateAddressLine2(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.addressLine2)
    }

    @Test
    fun `updateCity should update city`() = runTest {
        val newName = "Manchester"
        // ACTION
        viewModel.updateCity(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.city)
    }

    @Test
    fun `updateState should update state`() = runTest {
        val newName = "Greater Manchester"
        // ACTION
        viewModel.updateState(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.state)
    }

    @Test
    fun `updatePostalCode should update postalCode`() = runTest {
        val newName = "M11 5MW"
        // ACTION
        viewModel.updatePostalCode(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.postalCode)
    }

    @Test
    fun `updateCountry should update country`() = runTest {
        val newName = "United Kingdom"
        // ACTION
        viewModel.updateCountry(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.country)
    }
}
