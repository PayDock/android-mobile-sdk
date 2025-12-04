package com.paydock.sample.feature.wallet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paydock.feature.wallet.domain.model.integration.WalletType
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.core.EMAIL
import com.paydock.sample.core.FIRST_NAME
import com.paydock.sample.core.LAST_NAME
import com.paydock.sample.core.MERCHANT_NAME
import com.paydock.sample.feature.checkout.data.api.dto.ChargesCustomerDTO
import com.paydock.sample.feature.config.data.GlobalConfigRepository
import com.paydock.sample.feature.wallet.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.wallet.data.model.WalletCharge
import com.paydock.sample.feature.wallet.domain.usecase.InitiateWalletTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val initiateWalletTransactionUseCase: InitiateWalletTransactionUseCase,
    // GlobalConfigRepository provides access to global config values
    private val globalConfigRepository: GlobalConfigRepository
) : ViewModel() {

    // Get access token from global config (defaults to BuildConfig.ACCESS_TOKEN_API if not set)
    private suspend fun getAccessToken(): String {
        return globalConfigRepository.globalConfig.first().apiAccessToken
    }

    // Get cart amount from global config (defaults to AMOUNT if not set)
    private suspend fun getCartAmount(): BigDecimal {
        return globalConfigRepository.globalConfig.first().cartAmount
    }

    // Get cart currency from global config (defaults to AU_CURRENCY_CODE if not set)
    private suspend fun getCartCurrency(): String {
        return globalConfigRepository.globalConfig.first().cartCurrency
    }

    private val _stateFlow: MutableStateFlow<WalletTransactionUIState> =
        MutableStateFlow(WalletTransactionUIState())
    val stateFlow: StateFlow<WalletTransactionUIState> = _stateFlow

    private fun resetResultState() {
        _stateFlow.update { state ->
            state.copy(walletChargeResult = null, error = null)
        }
    }

    private fun initiateWalletTransactionResult(
        accessToken: String,
        manualCapture: Boolean = false,
        request: InitiateWalletRequest,
        callback: (Result<WalletTokenResult>) -> Unit,
    ) {
        viewModelScope.launch {
            // Loading is handled by the SDK
//            _stateFlow.update { state ->
//                state.copy(isLoading = true)
//            }
            val result =
                initiateWalletTransactionUseCase(
                    accessToken = accessToken,
                    manualCapture = manualCapture,
                    request = request
                )
            result.onSuccess { charge ->
                charge.walletToken?.let { callback(Result.success(WalletTokenResult(token = it))) }
                _stateFlow.update { state ->
                    state.copy(isLoading = false, error = null, walletChargeResult = charge)
                }
            }
            result.onFailure {
                callback(Result.failure(it))
            }
        }
    }

    /**
     * Creates a callback for wallet token requests.
     *
     * @param walletType The type of wallet payment method
     * @param customerData Optional customer data for the request
     * @param useGlobalConfig If true, uses global config values (access token, cart amount, currency).
     *                        If false, uses BuildConfig defaults. Defaults to true for widget flows.
     * @param overrideAccessToken Optional access token override. If provided, takes precedence over global config/defaults.
     * @param overrideAmount Optional amount override. If provided, takes precedence over global config/defaults.
     * @param overrideCurrency Optional currency override. If provided, takes precedence over global config/defaults.
     */
    fun getWalletTokenResultCallback(
        walletType: WalletType,
        customerData: CustomerData? = null,
        useGlobalConfig: Boolean = true,
        overrideAccessToken: String? = null,
        overrideAmount: BigDecimal? = null,
        overrideCurrency: String? = null
    ): (onTokenReceived: (Result<WalletTokenResult>) -> Unit) -> Unit =
        { onTokenReceived ->
            resetResultState()
            viewModelScope.launch {
                when (walletType) {
                    WalletType.AFTER_PAY -> {
                        val request = createAfterpayWalletRequest(
                            customerData = customerData,
                            useGlobalConfig = useGlobalConfig,
                            overrideAmount = overrideAmount,
                            overrideCurrency = overrideCurrency
                        )
                        initiateWalletTransactionResult(
                            accessToken = overrideAccessToken
                                ?: if (useGlobalConfig) getAccessToken() else BuildConfig.ACCESS_TOKEN_API,
                            request = request,
                            callback = onTokenReceived
                        )
                    }

                    WalletType.GOOGLE -> {
                        val request = createGoogleWalletRequest(
                            customerData = customerData,
                            useGlobalConfig = useGlobalConfig,
                            overrideAmount = overrideAmount,
                            overrideCurrency = overrideCurrency
                        )
                        initiateWalletTransactionResult(
                            accessToken = overrideAccessToken
                                ?: if (useGlobalConfig) getAccessToken() else BuildConfig.ACCESS_TOKEN_API,
                            request = request,
                            callback = onTokenReceived
                        )
                    }

                    WalletType.COLES_PAY -> {
                        val request = createColesPayWalletRequest(
                            customerData = customerData,
                            useGlobalConfig = useGlobalConfig,
                            overrideAmount = overrideAmount,
                            overrideCurrency = overrideCurrency
                        )
                        initiateWalletTransactionResult(
                            accessToken = overrideAccessToken
                                ?: if (useGlobalConfig) getAccessToken() else BuildConfig.ACCESS_TOKEN_API,
                            manualCapture = true,
                            request = request,
                            callback = onTokenReceived
                        )
                    }

                    WalletType.PAY_PAL -> {
                        val request = createPayPalWalletRequest(
                            customerData = customerData,
                            useGlobalConfig = useGlobalConfig,
                            overrideAmount = overrideAmount,
                            overrideCurrency = overrideCurrency
                        )
                        initiateWalletTransactionResult(
                            accessToken = overrideAccessToken
                                ?: if (useGlobalConfig) getAccessToken() else BuildConfig.ACCESS_TOKEN_API,
                            request = request,
                            callback = onTokenReceived
                        )
                    }
                }
            }
        }

    private suspend fun createPayPalWalletRequest(
        customerData: CustomerData? = null,
        useGlobalConfig: Boolean = true,
        overrideAmount: BigDecimal? = null,
        overrideCurrency: String? = null
    ): InitiateWalletRequest {
        val amount = customerData?.amount?.let { BigDecimal(it) }
            ?: overrideAmount
            ?: if (useGlobalConfig) getCartAmount() else BigDecimal(AMOUNT)
        val currency = overrideCurrency
            ?: if (useGlobalConfig) getCartCurrency() else AU_CURRENCY_CODE
        return InitiateWalletRequest(
            amount = amount,
            currency = currency,
            customer = ChargesCustomerDTO(
                firstName = customerData?.firstName ?: "Annaanna",
                lastName = customerData?.lastName ?: "Annaanna",
                email = customerData?.email ?: "annaanna@yopmail.com",
                paymentSource = ChargesCustomerDTO.PaymentSourceDTO(
                    gatewayId = BuildConfig.SERVICE_ID_PAYPAL,
                    addressLine1 = customerData?.billingAddress?.addressLine1 ?: "asd1",
                    addressLine2 = customerData?.billingAddress?.addressLine2,
                    addressLine3 = customerData?.billingAddress?.addressLine3,
                    city = customerData?.billingAddress?.city ?: "city",
                    state = customerData?.billingAddress?.state ?: "state",
                    countryCode = customerData?.billingAddress?.countryCode ?: "AU",
                    postalCode = customerData?.billingAddress?.postalCode ?: "12345",
                    walletType = WalletType.PAY_PAL.type
                )
            ),
            shippingDTO = customerData?.shippingAddress?.let {
                InitiateWalletRequest.ShippingDTO(
                    addressLine1 = it.addressLine1,
                    addressLine2 = it.addressLine2,
                    addressLine3 = it.addressLine3,
                    city = it.city,
                    state = it.state,
                    countryCode = it.countryCode,
                    postalCode = it.postalCode,
                    contact = InitiateWalletRequest.ShippingDTO.ContactDTO(
                        firstName = customerData.firstName,
                        lastName = customerData.lastName,
                        phone = customerData.phone
                    )
                )
            }
        )
    }

    private suspend fun createColesPayWalletRequest(
        customerData: CustomerData? = null,
        useGlobalConfig: Boolean = true,
        overrideAmount: BigDecimal? = null,
        overrideCurrency: String? = null
    ): InitiateWalletRequest {
        val amount = customerData?.amount?.let { BigDecimal(it) }
            ?: overrideAmount
            ?: if (useGlobalConfig) getCartAmount() else BigDecimal(AMOUNT)
        val currency = overrideCurrency
            ?: if (useGlobalConfig) getCartCurrency() else AU_CURRENCY_CODE
        return InitiateWalletRequest(
            amount = amount,
            currency = currency,
            customer = ChargesCustomerDTO(
                firstName = customerData?.firstName ?: FIRST_NAME,
                lastName = customerData?.lastName ?: LAST_NAME,
                email = customerData?.email ?: EMAIL,
                paymentSource = ChargesCustomerDTO.PaymentSourceDTO(
                    gatewayId = BuildConfig.SERVICE_ID_COLES_PAY,
                    addressLine1 = customerData?.billingAddress?.addressLine1 ?: "asd1",
                    addressLine2 = customerData?.billingAddress?.addressLine2,
                    addressLine3 = customerData?.billingAddress?.addressLine3,
                    city = customerData?.billingAddress?.city ?: "city",
                    state = customerData?.billingAddress?.state ?: "state",
                    countryCode = customerData?.billingAddress?.countryCode ?: "AU",
                    postalCode = customerData?.billingAddress?.postalCode ?: "12345",
                    walletType = WalletType.COLES_PAY.type
                )
            ),
            shippingDTO = customerData?.shippingAddress?.let {
                InitiateWalletRequest.ShippingDTO(
                    addressLine1 = it.addressLine1,
                    addressLine2 = it.addressLine2,
                    addressLine3 = it.addressLine3,
                    city = it.city,
                    state = it.state,
                    countryCode = it.countryCode,
                    postalCode = it.postalCode,
                    contact = InitiateWalletRequest.ShippingDTO.ContactDTO(
                        firstName = customerData.firstName,
                        lastName = customerData.lastName,
                        phone = customerData.phone
                    )
                )
            },
        )
    }

    private suspend fun createGoogleWalletRequest(
        customerData: CustomerData? = null,
        useGlobalConfig: Boolean = true,
        overrideAmount: BigDecimal? = null,
        overrideCurrency: String? = null
    ): InitiateWalletRequest {
        val amount = customerData?.amount?.let { BigDecimal(it) }
            ?: overrideAmount
            ?: if (useGlobalConfig) getCartAmount() else BigDecimal(AMOUNT)
        val currency = overrideCurrency
            ?: if (useGlobalConfig) getCartCurrency() else AU_CURRENCY_CODE
        return InitiateWalletRequest(
            amount = amount,
            currency = currency,
            customer = ChargesCustomerDTO(
                firstName = customerData?.firstName ?: FIRST_NAME,
                lastName = customerData?.lastName ?: LAST_NAME,
                email = customerData?.email ?: EMAIL,
                paymentSource = ChargesCustomerDTO.PaymentSourceDTO(
                    gatewayId = BuildConfig.SERVICE_ID_GOOGLE_PAY_MPGS,
                    addressLine1 = customerData?.billingAddress?.addressLine1 ?: "asd1",
                    addressLine2 = customerData?.billingAddress?.addressLine2,
                    addressLine3 = customerData?.billingAddress?.addressLine3,
                    city = customerData?.billingAddress?.city ?: "city",
                    state = customerData?.billingAddress?.state ?: "state",
                    countryCode = customerData?.billingAddress?.countryCode ?: "US",
                    postalCode = customerData?.billingAddress?.postalCode ?: "12345",
                    walletType = WalletType.GOOGLE.type
                )
            ),
            shippingDTO = customerData?.shippingAddress?.let {
                InitiateWalletRequest.ShippingDTO(
                    addressLine1 = it.addressLine1,
                    addressLine2 = it.addressLine2,
                    addressLine3 = it.addressLine3,
                    city = it.city,
                    state = it.state,
                    countryCode = it.countryCode,
                    postalCode = it.postalCode,
                    contact = InitiateWalletRequest.ShippingDTO.ContactDTO(
                        firstName = customerData.firstName,
                        lastName = customerData.lastName,
                        phone = customerData.phone
                    )
                )
            },
        )

    }

    private suspend fun createAfterpayWalletRequest(
        customerData: CustomerData? = null,
        useGlobalConfig: Boolean = true,
        overrideAmount: BigDecimal? = null,
        overrideCurrency: String? = null
    ): InitiateWalletRequest {
        val amount = customerData?.amount?.let { BigDecimal(it) }
            ?: overrideAmount
            ?: if (useGlobalConfig) getCartAmount() else BigDecimal(AMOUNT)
        val currency = overrideCurrency
            ?: if (useGlobalConfig) getCartCurrency() else AU_CURRENCY_CODE
        return InitiateWalletRequest(
            amount = amount,
            currency = currency,
            customer = ChargesCustomerDTO(
                firstName = customerData?.firstName ?: "David",
                lastName = customerData?.lastName ?: "Cameron",
                email = "david.cameron@paydock.com",
                paymentSource = ChargesCustomerDTO.PaymentSourceDTO(
                    gatewayId = BuildConfig.SERVICE_ID_AFTERPAY,
                    addressLine1 = customerData?.billingAddress?.addressLine1 ?: "asd1",
                    addressLine2 = customerData?.billingAddress?.addressLine2,
                    addressLine3 = customerData?.billingAddress?.addressLine3,
                    city = customerData?.billingAddress?.city ?: "city",
                    state = customerData?.billingAddress?.state ?: "state",
                    countryCode = customerData?.billingAddress?.countryCode ?: "AU",
                    postalCode = customerData?.billingAddress?.postalCode ?: "12345",
                    walletType = WalletType.AFTER_PAY.type
                ),
            ),
            meta = InitiateWalletRequest.MetaDTO(
                storeId = "1234",
                storeName = MERCHANT_NAME,
                successUrl = "https://paydock-integration.netlify.app/success",
                errorUrl = "https://paydock-integration.netlify.app/error"
            ),
            shippingDTO = customerData?.shippingAddress?.let {
                val shippingAmount = customerData.amount?.let { amount -> BigDecimal(amount) }
                    ?: overrideAmount
                    ?: if (useGlobalConfig) getCartAmount() else BigDecimal(AMOUNT)
                InitiateWalletRequest.ShippingDTO(
                    addressLine1 = it.addressLine1,
                    addressLine2 = it.addressLine2,
                    addressLine3 = it.addressLine3,
                    city = it.city,
                    state = it.state,
                    countryCode = it.countryCode,
                    postalCode = it.postalCode,
                    amount = shippingAmount,
                    contact = InitiateWalletRequest.ShippingDTO.ContactDTO(
                        firstName = customerData.firstName,
                        lastName = customerData.lastName,
                        phone = customerData.phone
                    )
                )
            } ?: InitiateWalletRequest.ShippingDTO(),
            itemDTOS = listOf(InitiateWalletRequest.ItemDTO()),
        )
    }
}

data class WalletTransactionUIState(
    val isLoading: Boolean = false,
    val walletChargeResult: WalletCharge? = null,
    val error: String? = null,
)

data class CustomerData(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val billingAddress: AddressData? = null,
    val shippingAddress: AddressData? = null,
    val amount: Double? = null
)

data class AddressData(
    val addressLine1: String,
    val addressLine2: String? = null,
    val addressLine3: String? = null,
    val city: String,
    val state: String,
    val postalCode: String,
    val countryCode: String
)