package com.paydock.feature.address.domain.mapper.integration

import android.location.Address
import com.paydock.core.BaseUnitTest
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

internal class MapperTest : BaseUnitTest() {

    @Test
    fun testAddressMappingWithFullStreet() {
        val mockAddress = mockk<Address>()

        every { mockAddress.featureName } returns "123"
        every { mockAddress.thoroughfare } returns "Main Street"
        every { mockAddress.locality } returns "City"
        every { mockAddress.adminArea } returns "State"
        every { mockAddress.postalCode } returns "12345"
        every { mockAddress.countryName } returns "Country"

        val billingAddress = mockAddress.asEntity()

        assertEquals("123 Main Street", billingAddress.addressLine1)
        assertEquals("City", billingAddress.city)
        assertEquals("State", billingAddress.state)
        assertEquals("12345", billingAddress.postalCode)
        assertEquals("Country", billingAddress.country)
    }

    @Test
    fun testAddressMappingWithOnlyStreetNumber() {
        val mockAddress = mockk<Address>()

        every { mockAddress.featureName } returns "456"
        every { mockAddress.thoroughfare } returns null
        every { mockAddress.locality } returns "City"
        every { mockAddress.adminArea } returns "State"
        every { mockAddress.postalCode } returns "67890"
        every { mockAddress.countryName } returns "Country"

        val billingAddress = mockAddress.asEntity()

        assertEquals("456", billingAddress.addressLine1)
        assertEquals("City", billingAddress.city)
        assertEquals("State", billingAddress.state)
        assertEquals("67890", billingAddress.postalCode)
        assertEquals("Country", billingAddress.country)
    }

    @Test
    fun testAddressMappingWithOnlyStreetName() {
        val mockAddress = mockk<Address>()

        every { mockAddress.featureName } returns null
        every { mockAddress.thoroughfare } returns "Park Avenue"
        every { mockAddress.locality } returns "City"
        every { mockAddress.adminArea } returns "State"
        every { mockAddress.postalCode } returns "54321"
        every { mockAddress.countryName } returns "Country"

        val billingAddress = mockAddress.asEntity()

        assertEquals("Park Avenue", billingAddress.addressLine1)
        assertEquals("City", billingAddress.city)
        assertEquals("State", billingAddress.state)
        assertEquals("54321", billingAddress.postalCode)
        assertEquals("Country", billingAddress.country)
    }

    @Test
    fun testAddressMappingWithNoStreetInfo() {
        val mockAddress = mockk<Address>()

        every { mockAddress.featureName } returns null
        every { mockAddress.thoroughfare } returns null
        every { mockAddress.locality } returns "City"
        every { mockAddress.adminArea } returns "State"
        every { mockAddress.postalCode } returns "98765"
        every { mockAddress.countryName } returns "Country"

        val billingAddress = mockAddress.asEntity()

        assertEquals("", billingAddress.addressLine1)
        assertEquals("City", billingAddress.city)
        assertEquals("State", billingAddress.state)
        assertEquals("98765", billingAddress.postalCode)
        assertEquals("Country", billingAddress.country)
    }

}