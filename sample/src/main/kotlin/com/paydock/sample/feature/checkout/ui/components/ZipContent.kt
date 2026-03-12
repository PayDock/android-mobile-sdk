package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.core.utils.toSafeAmount
import com.paydock.feature.zip.domain.model.ZipResult
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.feature.zip.presentation.ZipWidget
import com.paydock.feature.zip.presentation.ZipWidgetAppearanceDefaults
import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.checkout.presentation.EnhancedCheckoutViewModel
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.shop.data.CartManager
import java.math.BigDecimal
import java.util.Locale

@Composable
fun ZipContent(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loadingDelegate: WidgetLoadingDelegate?,
    viewModel: EnhancedCheckoutViewModel,
    resultHandler: (Result<ZipResult>) -> Unit
) {
    val configViewModel: ConfigViewModel = hiltViewModel()
    val globalConfig by configViewModel.globalConfig.collectAsState()

    // Get saved addresses to access firstName/lastName from selected addresses
    val savedAddresses by viewModel.savedAddresses.collectAsState()

    // Get cart information
    val cartManager = remember { CartManager.shared }
    val cartItems by cartManager.cartItems.collectAsState()
    val cartTotal = cartManager.totalPrice

    // Convert cart items to Zip widget items
    val zipItems = cartItems.map { cartItem ->
        ZipWidgetConfig.Item(
            name = cartItem.product.name,
            amount = cartItem.product.price.toString(),
            quantity = cartItem.quantity,
            reference = cartItem.product.id
        )
    }

    // Get shipping address name from saved address if selected, otherwise use contact info
    val selectedShippingAddress = viewModel.selectedShippingAddressId?.let { id ->
        savedAddresses.find { it.id == id }
    }
    val shippingFirstName = selectedShippingAddress?.firstName?.takeIf { it.isNotBlank() }
        ?: viewModel.contactInfo.firstName
    val shippingLastName = selectedShippingAddress?.lastName?.takeIf { it.isNotBlank() }
        ?: viewModel.contactInfo.lastName

    // Get billing address name:
    // - If using shipping as billing, use shipping address name
    // - Otherwise, use the selected billing address's saved address name if one is selected
    // - If no saved billing address is selected (manually entered), fall back to contact info
    val selectedBillingAddress = if (viewModel.useShippingAsBilling) {
        // When using shipping as billing, billing address uses shipping address's name
        selectedShippingAddress
    } else {
        // When NOT using shipping as billing, use billing address's own saved address name
        // Only look up if we have a selected billing address ID
        viewModel.selectedBillingAddressId?.let { id ->
            savedAddresses.find { it.id == id }
        }
        // If selectedBillingAddressId is null (manually entered address), selectedBillingAddress will be null
        // and we'll fall back to contact info below
    }

    // Use saved address name if available, otherwise fall back to contact info
    // Note: We only use shipping address name when useShippingAsBilling is true (handled above)
    val billingFirstName = selectedBillingAddress?.firstName?.takeIf { it.isNotBlank() }
        ?: viewModel.contactInfo.firstName
    val billingLastName = selectedBillingAddress?.lastName?.takeIf { it.isNotBlank() }
        ?: viewModel.contactInfo.lastName

    // Convert billing address from checkout view model
    val billingAddress = if (viewModel.billingAddress.isComplete) {
        ZipWidgetConfig.Address(
            firstName = billingFirstName,
            lastName = billingLastName,
            line1 = viewModel.billingAddress.addressLine1,
            line2 = viewModel.billingAddress.addressLine2?.takeIf { it.isNotBlank() },
            city = viewModel.billingAddress.city,
            state = viewModel.billingAddress.state,
            postcode = viewModel.billingAddress.postalCode,
            country = convertCountryNameToCode(viewModel.billingAddress.country)
        )
    } else null

    // Convert shipping address from checkout view model
    val shippingAddress = if (viewModel.shippingAddress.isComplete) {
        ZipWidgetConfig.Address(
            firstName = shippingFirstName,
            lastName = shippingLastName,
            line1 = viewModel.shippingAddress.addressLine1,
            line2 = viewModel.shippingAddress.addressLine2?.takeIf { it.isNotBlank() },
            city = viewModel.shippingAddress.city,
            state = viewModel.shippingAddress.state,
            postcode = viewModel.shippingAddress.postalCode,
            country = convertCountryNameToCode(viewModel.shippingAddress.country)
        )
    } else null

    ZipWidget(
        modifier = modifier,
        enabled = enabled,
        config = ZipWidgetConfig(
            accessToken = globalConfig.apiAccessToken,
            gatewayId = BuildConfig.SERVICE_ID_ZIP,
            amount = cartTotal.toSafeAmount(),
            currency = globalConfig.currencyCode,
            firstName = viewModel.contactInfo.firstName,
            lastName = viewModel.contactInfo.lastName,
            email = viewModel.contactInfo.email,
            phone = viewModel.contactInfo.phone.takeIf { it.isNotBlank() },
            billing = billingAddress,
            shipping = shippingAddress,
            items = zipItems.takeIf { it.isNotEmpty() }
        ),
        appearance = ZipWidgetAppearanceDefaults.appearance().copy(
            buttonStyle = ZipWidgetAppearanceDefaults.appearance().buttonStyle
        ),
        loadingDelegate = loadingDelegate,
        completion = resultHandler
    )
}

/**
 * Converts a country name to its ISO country code.
 * If the country name is not recognized, returns the input as-is (assumes it might already be a code).
 *
 * @param countryName The full country name (e.g., "United States", "Australia")
 * @return The ISO country code (e.g., "US", "AU") or the input if not found
 */
private fun convertCountryNameToCode(countryName: String): String {
    if (countryName.isBlank()) return countryName

    // Check if it's already a 2-letter code
    if (countryName.length == 2) return countryName.uppercase()

    // Find the ISO country code that matches the country name
    return Locale.getISOCountries().find { countryCode ->
        val locale = Locale.Builder().setRegion(countryCode).build()
        locale.displayCountry.equals(countryName, ignoreCase = true)
    } ?: countryName // Return original if not found
}
