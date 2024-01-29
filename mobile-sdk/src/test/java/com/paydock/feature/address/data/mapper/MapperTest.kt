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

package com.paydock.feature.address.data.mapper

import android.location.Address
import com.paydock.core.BaseUnitTest
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperTest : BaseUnitTest() {

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