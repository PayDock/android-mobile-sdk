package com.paydock.feature.afterpay.domain.mapper.integration

import com.afterpay.android.CancellationStatus
import com.afterpay.android.model.Money
import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption
import com.paydock.core.BaseUnitTest
import com.paydock.core.MobileSDKConstants
import com.paydock.feature.afterpay.domain.model.integration.AfterpaySDKConfig
import com.paydock.feature.afterpay.domain.model.integration.AfterpayShippingOption
import com.paydock.feature.afterpay.domain.model.integration.AfterpayShippingOptionUpdate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Currency

internal class MapperTest : BaseUnitTest() {

    @Test
    fun testShippingOptionsListMappingToShippingOptionsResult() {
        val shippingOptions = listOf(
            AfterpayShippingOption(
                "standard",
                "Standard",
                "",
                Currency.getInstance("AUD"),
                "0.00".toBigDecimal(),
                "50.00".toBigDecimal(),
                "0.00".toBigDecimal(),
            ),
            AfterpayShippingOption(
                "priority",
                "Priority",
                "Next business day",
                Currency.getInstance("AUD"),
                "10.00".toBigDecimal(),
                "60.00".toBigDecimal(),
                null,
            )
        )

        val result = shippingOptions.mapToSDKShippingOptionResult()

        assertEquals(shippingOptions.size, result.shippingOptions.size)
    }

    @Test
    fun testShippingOptionsListMappingToAfterpayShippingOptionsList() {
        val shippingOptions = listOf(
            AfterpayShippingOption(
                "standard",
                "Standard",
                "",
                Currency.getInstance("AUD"),
                "0.00".toBigDecimal(),
                "50.00".toBigDecimal(),
                "0.00".toBigDecimal(),
            ),
            AfterpayShippingOption(
                "priority",
                "Priority",
                "Next business day",
                Currency.getInstance("AUD"),
                "10.00".toBigDecimal(),
                "60.00".toBigDecimal(),
                null,
            )
        )

        val result = shippingOptions.mapToSDKShippingOptions()

        assertEquals(shippingOptions.size, result.size)
    }

    @Test
    fun testShippingOptionMappingToAfterpayShippingOption() {
        val shippingOption = AfterpayShippingOption(
            "standard",
            "Standard",
            "",
            Currency.getInstance("AUD"),
            "0.00".toBigDecimal(),
            "50.00".toBigDecimal(),
            "0.00".toBigDecimal(),
        )

        val result = shippingOption.mapToSDKShippingOption()

        assertEquals(shippingOption.id, result.id)
        assertEquals(shippingOption.name, result.name)
        assertEquals(shippingOption.description, result.description)
        assertEquals(shippingOption.orderAmount, result.orderAmount.amount)
        assertEquals(shippingOption.currency, result.orderAmount.currency)
        assertEquals(shippingOption.shippingAmount, result.shippingAmount.amount)
        assertEquals(shippingOption.currency, result.shippingAmount.currency)
        assertEquals(shippingOption.taxAmount, result.taxAmount?.amount)
        assertEquals(shippingOption.currency, result.taxAmount?.currency)
    }

    @Test
    fun testAfterpayShippingOptionMappingFromShippingOption() {
        val shippingOption = ShippingOption(
            "standard",
            "Standard",
            "",
            Money("0.00".toBigDecimal(), Currency.getInstance("AUD")),
            Money("50.00".toBigDecimal(), Currency.getInstance("AUD")),
            Money("0.00".toBigDecimal(), Currency.getInstance("AUD")),
        )

        val result = shippingOption.mapFromShippingOption()

        assertEquals(shippingOption.id, result.id)
        assertEquals(shippingOption.name, result.name)
        assertEquals(shippingOption.description, result.description)
        assertEquals(shippingOption.orderAmount.amount, result.orderAmount)
        assertEquals(shippingOption.orderAmount.currency, result.currency)
        assertEquals(shippingOption.shippingAmount.amount, result.shippingAmount)
        assertEquals(shippingOption.shippingAmount.currency, result.currency)
        assertEquals(shippingOption.taxAmount?.amount, result.taxAmount)
        assertEquals(shippingOption.taxAmount?.currency, result.currency)
    }

    @Test
    fun testShippingOptionUpdateMappingToShippingOptionUpdateResult() {
        val shippingOption = AfterpayShippingOptionUpdate(
            "standard",
            Currency.getInstance("AUD"),
            "0.00".toBigDecimal(),
            "50.00".toBigDecimal(),
            "2.00".toBigDecimal(),
        )

        val result = shippingOption.mapToSDKShippingOptionUpdateResult()

        assertEquals(shippingOption.id, result.shippingOptionUpdate.id)
        assertEquals(shippingOption.orderAmount, result.shippingOptionUpdate.orderAmount.amount)
        assertEquals(shippingOption.currency, result.shippingOptionUpdate.orderAmount.currency)
        assertEquals(
            shippingOption.shippingAmount,
            result.shippingOptionUpdate.shippingAmount.amount
        )
        assertEquals(shippingOption.currency, result.shippingOptionUpdate.shippingAmount.currency)
        assertEquals(shippingOption.taxAmount, result.shippingOptionUpdate.taxAmount?.amount)
        assertEquals(shippingOption.currency, result.shippingOptionUpdate.taxAmount?.currency)
    }

    @Test
    fun testShippingOptionUpdateMappingToAfterpayShippingOptionUpdate() {
        val shippingOption = AfterpayShippingOptionUpdate(
            "standard",
            Currency.getInstance("AUD"),
            "0.00".toBigDecimal(),
            "50.00".toBigDecimal(),
            "2.00".toBigDecimal(),
        )

        val result = shippingOption.mapToSDKShippingOptionUpdate()

        assertEquals(shippingOption.id, result.id)
        assertEquals(shippingOption.orderAmount, result.orderAmount.amount)
        assertEquals(shippingOption.currency, result.orderAmount.currency)
        assertEquals(shippingOption.shippingAmount, result.shippingAmount.amount)
        assertEquals(shippingOption.currency, result.shippingAmount.currency)
        assertEquals(shippingOption.taxAmount, result.taxAmount?.amount)
        assertEquals(shippingOption.currency, result.taxAmount?.currency)
    }

    @Test
    fun testAfterpayShippingAddressMappingFromBillingAddress() {
        val shippingAddress = ShippingAddress(
            "Test Address",
            "asd1",
            "asd2",
            "AU",
            "12345",
            "+5555555555",
            "state",
            "city"
        )

        val billingAddress = shippingAddress.mapFromBillingAddress()

        assertEquals(shippingAddress.name, billingAddress.name)
        assertEquals(shippingAddress.address1, billingAddress.addressLine1)
        assertEquals(shippingAddress.address2, billingAddress.addressLine2)
        assertEquals(shippingAddress.suburb, billingAddress.city)
        assertEquals(shippingAddress.state, billingAddress.state)
        assertEquals(shippingAddress.postcode, billingAddress.postalCode)
        assertEquals(shippingAddress.countryCode, billingAddress.countryCode)
        assertEquals(shippingAddress.phoneNumber, billingAddress.phoneNumber)
    }

    @Test
    fun testAfterpaySDKConfigOptionsMappingToAfterpayV2Options() {
        val configuration = AfterpaySDKConfig(
            config = AfterpaySDKConfig.AfterpayConfiguration(
                maximumAmount = "100",
                currency = "AUD",
                language = "en",
                country = "AU"
            ),
            options = AfterpaySDKConfig.CheckoutOptions(
                shippingOptionRequired = true,
                enableSingleShippingOptionUpdate = true
            )
        )

        val configOptions = configuration.options?.mapToAfterpayV2Options()

        assertEquals(configuration.options?.buyNow, configOptions?.buyNow)
        assertEquals(
            configuration.options?.shippingOptionRequired,
            configOptions?.shippingOptionRequired
        )
        assertEquals(configuration.options?.pickup, configOptions?.pickup)
        assertEquals(
            configuration.options?.enableSingleShippingOptionUpdate,
            configOptions?.enableSingleShippingOptionUpdate
        )
    }

    @Test
    fun testUserInitiatedCancellationMappingToString() {
        val message = CancellationStatus.USER_INITIATED.mapMessage()
        assertEquals(MobileSDKConstants.AfterpayConfig.USER_INITIATED_ERROR_MESSAGE, message)
    }

    @Test
    fun testNoCheckoutUrlCancellationMappingToString() {
        val message = CancellationStatus.NO_CHECKOUT_URL.mapMessage()
        assertEquals(MobileSDKConstants.AfterpayConfig.NO_CHECKOUT_URL_ERROR_MESSAGE, message)
    }

    @Test
    fun testInvalidCheckoutUrlCancellationMappingToString() {
        val message = CancellationStatus.INVALID_CHECKOUT_URL.mapMessage()
        assertEquals(MobileSDKConstants.AfterpayConfig.INVALID_CHECKOUT_URL_ERROR_MESSAGE, message)
    }

    @Test
    fun testNoCheckoutHandlerCancellationMappingToString() {
        val message = CancellationStatus.NO_CHECKOUT_HANDLER.mapMessage()
        assertEquals(MobileSDKConstants.AfterpayConfig.NO_CHECKOUT_HANDLER_ERROR_MESSAGE, message)
    }

    @Test
    fun testNoConfigurationCancellationMappingToString() {
        val message = CancellationStatus.NO_CONFIGURATION.mapMessage()
        assertEquals(MobileSDKConstants.AfterpayConfig.NO_CONFIGURATION_ERROR_MESSAGE, message)
    }

    @Test
    fun testLanguageNotSupportedCancellationMappingToString() {
        val message = CancellationStatus.LANGUAGE_NOT_SUPPORTED.mapMessage()
        assertEquals(MobileSDKConstants.AfterpayConfig.LANGUAGE_NOT_SUPPORTED_ERROR_MESSAGE, message)
    }
}