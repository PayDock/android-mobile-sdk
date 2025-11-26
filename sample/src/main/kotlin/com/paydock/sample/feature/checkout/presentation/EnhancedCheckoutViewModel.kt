package com.paydock.sample.feature.checkout.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paydock.feature.card.domain.model.integration.CardResult
import com.paydock.feature.threeDS.integrated.domain.model.integration.Integrated3DSResult
import com.paydock.feature.threeDS.integrated.domain.model.integration.enums.IntegratedEventType
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.StandaloneEventType
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.TOKENISE_CLICK_TO_PAY_ERROR
import com.paydock.sample.core.WALLET_CHARGE_TRANSACTION_ERROR
import com.paydock.sample.feature.account.data.UserProfileManager
import com.paydock.sample.feature.card.data.api.dto.VaultTokenRequest
import com.paydock.sample.feature.card.domain.usecase.CaptureCardChargeTokenUseCase
import com.paydock.sample.feature.card.domain.usecase.CreateCardSessionVaultTokenUseCase
import com.paydock.sample.feature.checkout.data.api.dto.ChargesCustomerDTO
import com.paydock.sample.feature.checkout.domain.model.Address
import com.paydock.sample.feature.checkout.domain.model.AddressType
import com.paydock.sample.feature.checkout.domain.model.CheckoutStep
import com.paydock.sample.feature.checkout.domain.model.ContactInfo
import com.paydock.sample.feature.checkout.domain.model.PaymentMethod
import com.paydock.sample.feature.checkout.domain.model.SavedAddress
import com.paydock.sample.feature.checkout.models.ThreeDSType
import com.paydock.sample.feature.shop.data.CartManager
import com.paydock.sample.feature.threeDS.data.api.dto.Capture3DSChargeRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateIntegratedThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.domain.model.ThreeDSToken
import com.paydock.sample.feature.threeDS.domain.usecase.CaptureThreeDSChargeTokenUseCase
import com.paydock.sample.feature.threeDS.domain.usecase.CreateIntegratedThreeDSTokenUseCase
import com.paydock.sample.feature.threeDS.domain.usecase.CreateStandaloneThreeDSTokenUseCase
import com.paydock.sample.feature.wallet.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.sample.feature.wallet.presentation.AddressData
import com.paydock.sample.feature.wallet.presentation.CustomerData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import com.paydock.sample.feature.account.domain.model.SavedAddress as ProfileSavedAddress

@HiltViewModel
class EnhancedCheckoutViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val createCardSessionVaultTokenUseCase: CreateCardSessionVaultTokenUseCase,
    private val createIntegratedThreeDSTokenUseCase: CreateIntegratedThreeDSTokenUseCase,
    private val createStandaloneThreeDSTokenUseCase: CreateStandaloneThreeDSTokenUseCase,
    private val captureThreeDSChargeTokenUseCase: CaptureThreeDSChargeTokenUseCase,
    ) : ViewModel() {

    private val profileManager = UserProfileManager.shared

    // Current step - persisted to survive process death with "Don't keep activities"
    private val _currentStep = MutableStateFlow(
        savedStateHandle[KEY_CURRENT_STEP] ?: CheckoutStep.INFORMATION
    )
    val currentStep: StateFlow<CheckoutStep> = _currentStep.asStateFlow()

    // Contact Information
    var contactInfo by mutableStateOf(ContactInfo())
        private set

    // Addresses
    var shippingAddress by mutableStateOf(Address())
        private set

    var billingAddress by mutableStateOf(Address())
        private set

    var useShippingAsBilling by mutableStateOf(true)
        private set

    var selectedShippingAddressId by mutableStateOf<String?>(null)
        private set

    var selectedBillingAddressId by mutableStateOf<String?>(null)
        private set

    // Payment - persisted to survive process death with "Don't keep activities"
    var selectedPaymentMethod by mutableStateOf<PaymentMethod?>(
        savedStateHandle[KEY_PAYMENT_METHOD]
    )
        private set

    var paymentToken by mutableStateOf<String?>(null)
        private set

    // 3DS state
    var threeDSType: ThreeDSType = ThreeDSType.STANDALONE
        private set

    var vaultToken by mutableStateOf<String?>(null)
        private set

    var threeDSToken by mutableStateOf<String?>(null)
        private set

    // UI State
    var showAddressWidget by mutableStateOf(false)
        private set

    var addressWidgetType by mutableStateOf(AddressType.SHIPPING)
        private set

    var addressWidgetInitialAddress by mutableStateOf<Address?>(null)
        private set
    var addressWidgetInitialFirstName by mutableStateOf<String?>(null)
        private set
    var addressWidgetInitialLastName by mutableStateOf<String?>(null)
        private set
    var addressWidgetInitialLabel by mutableStateOf<String?>(null)
        private set
    var editingSavedAddressId by mutableStateOf<String?>(null)
        private set

    var showAlert by mutableStateOf(false)
        private set

    var alertTitle by mutableStateOf("")
        private set

    var alertMessage by mutableStateOf("")
        private set

    var orderCompleted by mutableStateOf(false)
        private set

    var orderFailed by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    // Reset key to force fresh payment widget instances
    var paymentWidgetResetKey by mutableIntStateOf(0)
        private set

    // Get saved addresses from profile manager - converted to StateFlow for reactivity
    val savedAddresses: StateFlow<List<SavedAddress>> = profileManager.profile.map { profile ->
        profile.savedAddresses.map { profileAddress ->
            SavedAddress(
                id = profileAddress.id,
                label = profileAddress.label,
                address = Address(
                    addressLine1 = profileAddress.addressLine1,
                    addressLine2 = profileAddress.addressLine2,
                    city = profileAddress.city,
                    state = profileAddress.state,
                    postalCode = profileAddress.postalCode,
                    country = profileAddress.country
                ),
                isDefault = profileAddress.isDefault,
                firstName = profileAddress.firstName,
                lastName = profileAddress.lastName
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Computed properties
    val isInformationComplete: Boolean
        get() = contactInfo.isComplete &&
                shippingAddress.isComplete &&
                (useShippingAsBilling || billingAddress.isComplete)

    val canProceed: Boolean
        get() = when (_currentStep.value) {
            CheckoutStep.INFORMATION -> isInformationComplete
            CheckoutStep.PAYMENT -> selectedPaymentMethod != null
        }

    val actionButtonTitle: String
        get() = when (_currentStep.value) {
            CheckoutStep.INFORMATION -> "Continue to Payment"
            CheckoutStep.PAYMENT -> "Place Order"
        }

    // Actions
    fun updateContactInfo(
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        phone: String? = null
    ) {
        contactInfo = contactInfo.copy(
            firstName = firstName ?: contactInfo.firstName,
            lastName = lastName ?: contactInfo.lastName,
            email = email ?: contactInfo.email,
            phone = phone ?: contactInfo.phone
        )
    }

    fun updateShippingAddress(address: Address) {
        shippingAddress = address
        selectedShippingAddressId = null
        if (useShippingAsBilling) {
            billingAddress = address
        }
    }

    fun updateBillingAddress(address: Address) {
        billingAddress = address
        selectedBillingAddressId = null
    }

    fun setShippingAsBilling(use: Boolean) {
        useShippingAsBilling = use
        if (use) {
            billingAddress = shippingAddress
            selectedBillingAddressId = null
        }
    }

    fun selectSavedAddress(savedAddress: SavedAddress, type: AddressType) {
        when (type) {
            AddressType.SHIPPING -> {
                selectedShippingAddressId = savedAddress.id
                shippingAddress = savedAddress.address
                if (useShippingAsBilling) {
                    billingAddress = savedAddress.address
                }
            }

            AddressType.BILLING -> {
                selectedBillingAddressId = savedAddress.id
                billingAddress = savedAddress.address
            }
        }
    }

    fun saveAddress(address: ProfileSavedAddress) {
        profileManager.addAddress(address)
    }

    fun updateAddress(address: ProfileSavedAddress) {
        profileManager.updateAddress(address)
    }

    fun setPaymentMethod(method: PaymentMethod) {
        selectedPaymentMethod = method
        savedStateHandle[KEY_PAYMENT_METHOD] = method
        // Increment reset key to force fresh widget instance
        paymentWidgetResetKey++
    }

    fun setThePaymentToken(token: String) {
        paymentToken = token
    }

    fun setIsLoading(loading: Boolean) {
        isLoading = loading
    }

    fun routeToFailure() {
        isLoading = false
        orderCompleted = false
        orderFailed = true
        // Increment reset key to force fresh widget on retry
        paymentWidgetResetKey++
        setCurrentStep(CheckoutStep.PAYMENT)
    }

    fun showAddressWidget(
        type: AddressType,
        initialAddress: Address? = null,
        initialFirstName: String? = null,
        initialLastName: String? = null
    ) {
        addressWidgetType = type
        addressWidgetInitialAddress = initialAddress
        addressWidgetInitialFirstName = initialFirstName
        addressWidgetInitialLastName = initialLastName
        showAddressWidget = true
    }

    fun startEditingSavedAddress(saved: SavedAddress, type: AddressType) {
        editingSavedAddressId = saved.id
        showAddressWidget(
            type = type,
            initialAddress = saved.address,
            initialFirstName = saved.firstName,
            initialLastName = saved.lastName
        )
        addressWidgetInitialLabel = saved.label
    }

    fun hideAddressWidget() {
        showAddressWidget = false
        addressWidgetInitialAddress = null
        addressWidgetInitialFirstName = null
        addressWidgetInitialLastName = null
        addressWidgetInitialLabel = null
        editingSavedAddressId = null
    }

    fun setCurrentStep(step: CheckoutStep) {
        _currentStep.value = step
        savedStateHandle[KEY_CURRENT_STEP] = step
    }

    fun goToNextStep() {
        val currentIndex = CheckoutStep.getAllSteps().indexOf(_currentStep.value)
        if (currentIndex < CheckoutStep.getAllSteps().size - 1) {
            val nextStep = CheckoutStep.getAllSteps()[currentIndex + 1]
            _currentStep.value = nextStep
            savedStateHandle[KEY_CURRENT_STEP] = nextStep
        }
    }

    fun goToPreviousStep() {
        val currentIndex = CheckoutStep.getAllSteps().indexOf(_currentStep.value)
        if (currentIndex > 0) {
            val previousStep = CheckoutStep.getAllSteps()[currentIndex - 1]
            _currentStep.value = previousStep
            savedStateHandle[KEY_CURRENT_STEP] = previousStep
        }
    }

    fun handleActionButton() {
        when (_currentStep.value) {
            CheckoutStep.INFORMATION -> goToNextStep()
            CheckoutStep.PAYMENT -> placeOrder()
        }
    }

    fun placeOrder() {
        viewModelScope.launch {
            isLoading = true
            try {
                orderCompleted = true
                // Navigation to confirmation screen will be handled by LaunchedEffect in EnhancedCheckoutScreen

            } catch (_: Exception) {
                orderFailed = true
                // Navigation to confirmation screen will be handled by LaunchedEffect in EnhancedCheckoutScreen
            } finally {
                isLoading = false
            }
        }
    }

    // --- Card + 3DS flow (extracted from Standalone) ---
    fun handleCardResult(result: Result<CardResult>) {
        result.onSuccess {
            createSessionVaultToken(it.token)
        }.onFailure {
            routeToFailure()
        }
    }

    // --- ClickToPay + 3DS flow ---
    fun handleClickToPayResult(result: Result<String>) {
        result.onSuccess {
            createSessionVaultToken(cardToken = it)
        }.onFailure {
            routeToFailure()
        }
    }

    private fun createSessionVaultToken(cardToken: String) {
        viewModelScope.launch {
            isLoading = true
            val request = VaultTokenRequest.CreateCardSessionVaultTokenRequest(token = cardToken)
            val result = createCardSessionVaultTokenUseCase(request)
            result.onSuccess { token ->
                vaultToken = token
                when (threeDSType) {
                    ThreeDSType.INTEGRATED -> createIntegrated3dsToken(token)
                    ThreeDSType.STANDALONE -> createStandalone3dsToken(token)
                }
            }.onFailure {
                routeToFailure()
            }
        }
    }

    private fun createIntegrated3dsToken(vaultToken: String) {
        viewModelScope.launch {
            val result = createIntegratedThreeDSTokenUseCase(
                request = CreateIntegratedThreeDSTokenRequest(
                    customer = ChargesCustomerDTO(
                        paymentSource = ChargesCustomerDTO.PaymentSourceDTO(
                            gatewayId = BuildConfig.SERVICE_ID_MPGS,
                            vaultToken = vaultToken
                        )
                    )
                )
            )
            handle3DSTokenResult(result)
        }
    }

    private fun createStandalone3dsToken(vaultToken: String) {
        viewModelScope.launch {
            val result = createStandaloneThreeDSTokenUseCase(
                CreateStandaloneThreeDSTokenRequest(
                    customer = ChargesCustomerDTO(
                        paymentSource = ChargesCustomerDTO.PaymentSourceDTO(vaultToken = vaultToken)
                    )
                )
            )
            handle3DSTokenResult(result)
        }
    }

    private fun handle3DSTokenResult(result: Result<ThreeDSToken>) {
        result.onSuccess { threeDSResult ->
            val status = threeDSResult.status
            val hasToken = !threeDSResult.token.isNullOrBlank()
            val hasId = !threeDSResult.id.isNullOrBlank()

            when {
                // Explicit NOT_SUPPORTED: proceed to capture if we have an id
                status == ThreeDSToken.ThreeDSStatus.NOT_SUPPORTED && hasId -> {
                    when (threeDSType) {
                        ThreeDSType.INTEGRATED -> captureIntegrated3DSCharge(threeDSResult.id)
                        ThreeDSType.STANDALONE -> captureStandalone3DSCharge(threeDSResult.id)
                    }
                }

                // Explicit PRE_AUTH_PENDING: open 3DS flow if token present
                status == ThreeDSToken.ThreeDSStatus.PRE_AUTH_PENDING && hasToken -> {
                    threeDSToken = threeDSResult.token
                    // Dismiss payment footer when 3DS flow starts
                    selectedPaymentMethod = null
                    savedStateHandle[KEY_PAYMENT_METHOD] = null
                    isLoading = false
                }

                // Fallbacks to be resilient to backend status naming:
                // If a token is present, open 3DS regardless of status string
                hasToken -> {
                    threeDSToken = threeDSResult.token
                    // Dismiss payment footer when 3DS flow starts
                    selectedPaymentMethod = null
                    savedStateHandle[KEY_PAYMENT_METHOD] = null
                    isLoading = false
                }

                // If no token but an id exists, attempt capture (provider completed silently)
                hasId -> {
                    when (threeDSType) {
                        ThreeDSType.INTEGRATED -> captureIntegrated3DSCharge(threeDSResult.id)
                        ThreeDSType.STANDALONE -> captureStandalone3DSCharge(threeDSResult.id)
                    }
                }

                // Otherwise treat as failure
                else -> {
                    routeToFailure()
                }
            }
        }.onFailure {
            routeToFailure()
        }
    }

    fun handleIntegrated3DSResult(result: Result<Integrated3DSResult>) {
        result.onSuccess {
            if (it.event == IntegratedEventType.CHARGE_AUTH_SUCCESS) {
                it.charge3dsId?.let { id -> captureIntegrated3DSCharge(id) }
                threeDSToken = null
            } else if (it.event == IntegratedEventType.CHARGE_AUTH_REJECT) {
                isLoading = false
                threeDSToken = null
                vaultToken = null
                paymentToken = null
                selectedPaymentMethod = null
                savedStateHandle[KEY_PAYMENT_METHOD] = null
                orderFailed = true
                paymentWidgetResetKey++
            }
        }.onFailure {
            isLoading = false
            threeDSToken = null
            vaultToken = null
            paymentToken = null
            selectedPaymentMethod = null
            savedStateHandle[KEY_PAYMENT_METHOD] = null
            orderCompleted = false
            paymentWidgetResetKey++
        }
    }

    fun handleStandalone3DSResult(result: Result<Standalone3DSResult>) {
        result.onSuccess {
            if (it.event == StandaloneEventType.CHARGE_AUTH_SUCCESS) {
                it.charge3dsId?.let { id -> captureStandalone3DSCharge(id) }
                threeDSToken = null
            } else if (it.event == StandaloneEventType.CHARGE_AUTH_REJECT ||
                it.event == StandaloneEventType.CHARGE_ERROR
            ) {
                isLoading = false
                threeDSToken = null
                vaultToken = null
                paymentToken = null
                selectedPaymentMethod = null
                savedStateHandle[KEY_PAYMENT_METHOD] = null
                orderFailed = true
                paymentWidgetResetKey++
            }
        }.onFailure {
            isLoading = false
            threeDSToken = null
            vaultToken = null
            paymentToken = null
            selectedPaymentMethod = null
            savedStateHandle[KEY_PAYMENT_METHOD] = null
            orderCompleted = false
            paymentWidgetResetKey++
        }
    }

    private fun captureIntegrated3DSCharge(threeDSChargeId: String) {
        viewModelScope.launch {
            isLoading = true
            val request = Capture3DSChargeRequest.CaptureIntegrated3DSChargeRequest(
                threeDSData = Capture3DSChargeRequest.CaptureIntegrated3DSChargeRequest.ThreeDSChargeData(
                    threeDSChargeId
                )
            )
            val result = captureThreeDSChargeTokenUseCase(request)
            result.onSuccess {
                isLoading = false
                orderCompleted = true
            }.onFailure {
                isLoading = false
                threeDSToken = null
                vaultToken = null
                paymentToken = null
                selectedPaymentMethod = null
                savedStateHandle[KEY_PAYMENT_METHOD] = null
                orderFailed = true
                paymentWidgetResetKey++
            }
        }
    }

    private fun captureStandalone3DSCharge(threeDSChargeId: String) {
        viewModelScope.launch {
            isLoading = true
            val vault = vaultToken
            val request = Capture3DSChargeRequest.CaptureStandalone3DSChargeRequest(
                threeDSChargeId = threeDSChargeId,
                customer = ChargesCustomerDTO(
                    paymentSource = ChargesCustomerDTO.PaymentSourceDTO(
                        gatewayId = BuildConfig.SERVICE_ID_MPGS,
                        vaultToken = vault
                    )
                )
            )
            val result = captureThreeDSChargeTokenUseCase(request)
            result.onSuccess {
                isLoading = false
                orderCompleted = true
            }.onFailure {
                isLoading = false
                threeDSToken = null
                vaultToken = null
                paymentToken = null
                selectedPaymentMethod = null
                savedStateHandle[KEY_PAYMENT_METHOD] = null
                orderFailed = true
                paymentWidgetResetKey++
            }
        }
    }

    fun dismissAlert() {
        showAlert = false
        if (orderCompleted) {
            // Reset checkout state
            resetCheckout()
        }
    }

    fun resetCheckout() {
        _currentStep.value = CheckoutStep.INFORMATION
        savedStateHandle[KEY_CURRENT_STEP] = CheckoutStep.INFORMATION
        contactInfo = ContactInfo()
        shippingAddress = Address()
        billingAddress = Address()
        useShippingAsBilling = true
        selectedShippingAddressId = null
        selectedBillingAddressId = null
        selectedPaymentMethod = null
        savedStateHandle[KEY_PAYMENT_METHOD] = null
        paymentToken = null
        orderCompleted = false
        orderFailed = false
    }

    fun loadProfileData() {
        viewModelScope.launch {
            // Load from UserProfileManager
            val profile = profileManager.profile.value
            contactInfo = ContactInfo(
                firstName = profile.firstName,
                lastName = profile.lastName,
                email = profile.email,
                phone = profile.phone
            )

            // Auto-select default address if available
            profile.savedAddresses.find { it.isDefault }?.let { defaultAddress ->
                val checkoutAddress = Address(
                    addressLine1 = defaultAddress.addressLine1,
                    addressLine2 = defaultAddress.addressLine2,
                    city = defaultAddress.city,
                    state = defaultAddress.state,
                    postalCode = defaultAddress.postalCode,
                    country = defaultAddress.country
                )
                shippingAddress = checkoutAddress
                billingAddress = checkoutAddress
            }
        }
    }

    // Provide customer data for wallet requests
    fun getCustomerDataForWallet(): CustomerData {
        // Get the cart total from CartManager for checkout flow
        val cartTotal = CartManager.shared.totalPrice

        return CustomerData(
            firstName = contactInfo.firstName,
            lastName = contactInfo.lastName,
            email = contactInfo.email,
            phone = contactInfo.phone.takeIf { it.isNotBlank() },
            billingAddress = if (billingAddress.isComplete) {
                AddressData(
                    addressLine1 = billingAddress.addressLine1,
                    addressLine2 = billingAddress.addressLine2?.takeIf { it.isNotBlank() },
                    addressLine3 = null,
                    city = billingAddress.city,
                    state = billingAddress.state,
                    postalCode = billingAddress.postalCode,
                    countryCode = convertCountryNameToCode(billingAddress.country)
                )
            } else null,
            shippingAddress = if (shippingAddress.isComplete) {
                AddressData(
                    addressLine1 = shippingAddress.addressLine1,
                    addressLine2 = shippingAddress.addressLine2?.takeIf { it.isNotBlank() },
                    addressLine3 = null,
                    city = shippingAddress.city,
                    state = shippingAddress.state,
                    postalCode = shippingAddress.postalCode,
                    countryCode = convertCountryNameToCode(shippingAddress.country)
                )
            } else null,
            amount = cartTotal
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

    private companion object {
        const val KEY_CURRENT_STEP = "checkout.current_step"
        const val KEY_PAYMENT_METHOD = "checkout.payment_method"
    }
}