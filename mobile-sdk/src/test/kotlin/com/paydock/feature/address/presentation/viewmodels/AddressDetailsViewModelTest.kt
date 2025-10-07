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
    fun `updateFirstName should update firstName`() = runTest {
        val newName = "John"
        // ACTION
        viewModel.updateFirstName(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.formState.firstName)
    }

    @Test
    fun `updateLastName should update lastName`() = runTest {
        val newName = "Doe"
        // ACTION
        viewModel.updateLastName(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.formState.lastName)
    }

    @Test
    fun `updateAddressLine1 should update addressLine1`() = runTest {
        val newName = "1 Park Avenue"
        // ACTION
        viewModel.updateAddressLine1(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.formState.addressLine1)
    }

    @Test
    fun `updateAddressLine2 should update addressLine2`() = runTest {
        val newName = "2 Park Avenue"
        // ACTION
        viewModel.updateAddressLine2(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.formState.addressLine2)
    }

    @Test
    fun `updateCity should update city`() = runTest {
        val newName = "Manchester"
        // ACTION
        viewModel.updateCity(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.formState.city)
    }

    @Test
    fun `updateState should update state`() = runTest {
        val newName = "Greater Manchester"
        // ACTION
        viewModel.updateState(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.formState.state)
    }

    @Test
    fun `updatePostalCode should update postalCode`() = runTest {
        val newName = "M11 5MW"
        // ACTION
        viewModel.updatePostalCode(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.formState.postalCode)
    }

    @Test
    fun `updateCountry should update country`() = runTest {
        val newName = "United Kingdom"
        // ACTION
        viewModel.updateCountry(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.formState.country)
    }

    @Test
    fun `updateDefaultAddress should set address details view state`() = runTest {
        val savedAddress = BillingAddress(
            firstName = "John",
            lastName = "Doe",
            addressLine1 = "1 Park Avenue",
            city = "Manchester",
            state = "Greater Manchester",
            postalCode = "M11 5MW",
            country = "United Kingdom"
        )
        // ACTION
        viewModel.populateFormWithBillingAddress(savedAddress)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertEquals(savedAddress.firstName, state.formState.firstName)
        assertEquals(savedAddress.lastName, state.formState.lastName)
        assertEquals(savedAddress.addressLine1, state.formState.addressLine1)
        assertEquals(savedAddress.addressLine2 ?: "", state.formState.addressLine2)
        assertEquals(savedAddress.city, state.formState.city)
        assertEquals(savedAddress.state, state.formState.state)
        assertEquals(savedAddress.postalCode, state.formState.postalCode)
        assertEquals(savedAddress.country, state.formState.country)
    }

    @Test
    fun `updateManualAddress should update address details view state`() = runTest {
        // ACTION
        // Update manual input items
        viewModel.updateFirstName("John")
        viewModel.updateLastName("Doe")
        viewModel.updateAddressLine1("1 Park Avenue")
        viewModel.updateCity("Manchester")
        viewModel.updateState("Greater Manchester")
        viewModel.updatePostalCode("M11 5MW")
        viewModel.updateCountry("United Kingdom")
        // CHECK
        val state = viewModel.stateFlow.first()
        assertEquals("John", state.formState.firstName)
        assertEquals("Doe", state.formState.lastName)
        assertEquals("1 Park Avenue", state.formState.addressLine1)
        assertEquals("Manchester", state.formState.city)
        assertEquals("Greater Manchester", state.formState.state)
        assertEquals("M11 5MW", state.formState.postalCode)
        assertEquals("United Kingdom", state.formState.country)
    }
}
