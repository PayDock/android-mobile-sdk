package com.paydock.feature.afterpay.presentation.utils

import com.afterpay.android.model.Money
import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption
import com.afterpay.android.model.ShippingOptionUpdateResult
import com.afterpay.android.model.ShippingOptionsResult
import com.paydock.feature.afterpay.presentation.mapper.mapToSDKShippingOptionResult
import com.paydock.feature.afterpay.presentation.model.AfterpayShippingOption
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.util.Currency

@RunWith(MockitoJUnitRunner::class)
class CheckoutHandlerTest {

    @Mock
    lateinit var onDidCommenceCheckout: () -> Unit

    @Mock
    lateinit var onShippingAddressDidChange: (ShippingAddress) -> Unit

    @Mock
    lateinit var onShippingOptionDidChange: (ShippingOption) -> Unit

    @Mock
    lateinit var onTokenLoaded: (Result<String>) -> Unit

    @Mock
    lateinit var onProvideShippingOptions: (ShippingOptionsResult) -> Unit

    @Mock
    lateinit var onProvideShippingOptionUpdate: (ShippingOptionUpdateResult?) -> Unit

    @Test
    fun `test didCommenceCheckout`() {
        val checkoutHandler = CheckoutHandler(
            onDidCommenceCheckout,
            onShippingAddressDidChange,
            onShippingOptionDidChange
        )
        checkoutHandler.didCommenceCheckout(onTokenLoaded)

        verify(onDidCommenceCheckout).invoke()
    }

    @Test
    fun `test provideTokenResult`() {
        val checkoutHandler = CheckoutHandler(
            onDidCommenceCheckout,
            onShippingAddressDidChange,
            onShippingOptionDidChange
        )
        // simulate trigger from SDK
        checkoutHandler.didCommenceCheckout(onTokenLoaded)

        val result: Result<String> = Result.success("token")
        checkoutHandler.provideTokenResult(result)

        verify(onTokenLoaded).invoke(result)
    }

    @Test
    fun `test shippingAddressDidChange`() {
        val checkoutHandler = CheckoutHandler(
            onDidCommenceCheckout,
            onShippingAddressDidChange,
            onShippingOptionDidChange
        )
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
        checkoutHandler.shippingAddressDidChange(shippingAddress, onProvideShippingOptions)

        verify(onShippingAddressDidChange).invoke(shippingAddress)
    }

    @Test
    fun `test provideShippingOptionsResult`() {
        val checkoutHandler = CheckoutHandler(
            onDidCommenceCheckout,
            onShippingAddressDidChange,
            onShippingOptionDidChange
        )
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
        checkoutHandler.shippingAddressDidChange(shippingAddress, onProvideShippingOptions)

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
        val shippingOptionsResult = shippingOptions.mapToSDKShippingOptionResult()
        checkoutHandler.provideShippingOptionsResult(shippingOptionsResult)

        verify(onProvideShippingOptions).invoke(shippingOptionsResult)
    }

    @Test
    fun `test shippingOptionDidChange`() {
        val checkoutHandler = CheckoutHandler(
            onDidCommenceCheckout,
            onShippingAddressDidChange,
            onShippingOptionDidChange
        )
        val shippingOption = ShippingOption(
            "standard",
            "Standard",
            "",
            Money("0.00".toBigDecimal(), Currency.getInstance("AUD")),
            Money("50.00".toBigDecimal(), Currency.getInstance("AUD")),
            Money("0.00".toBigDecimal(), Currency.getInstance("AUD")),
        )
        checkoutHandler.shippingOptionDidChange(shippingOption, onProvideShippingOptionUpdate)

        verify(onShippingOptionDidChange).invoke(shippingOption)
    }

    @Test
    fun `test provideShippingOptionUpdateResult`() {
        val checkoutHandler = CheckoutHandler(
            onDidCommenceCheckout,
            onShippingAddressDidChange,
            onShippingOptionDidChange
        )
        val shippingOption = ShippingOption(
            "standard",
            "Standard",
            "",
            Money("0.00".toBigDecimal(), Currency.getInstance("AUD")),
            Money("50.00".toBigDecimal(), Currency.getInstance("AUD")),
            Money("0.00".toBigDecimal(), Currency.getInstance("AUD")),
        )
        checkoutHandler.shippingOptionDidChange(shippingOption, onProvideShippingOptionUpdate)

        val shippingOptionUpdateResult: ShippingOptionUpdateResult? = null
        checkoutHandler.provideShippingOptionUpdateResult(shippingOptionUpdateResult)

        verify(onProvideShippingOptionUpdate).invoke(shippingOptionUpdateResult)
    }
}