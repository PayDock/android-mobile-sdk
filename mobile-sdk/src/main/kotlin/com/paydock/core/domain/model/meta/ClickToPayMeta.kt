package com.paydock.core.domain.model.meta

import com.paydock.core.domain.model.meta.base.BaseSRCMeta
import com.paydock.core.domain.model.meta.enum.CardBrands
import com.paydock.core.domain.model.meta.enum.CheckoutExperience
import com.paydock.core.domain.model.meta.enum.Services
import com.paydock.core.domain.model.meta.enum.UnacceptedCardType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data used for the ClickToPay Checkout. For further information,
 * refer to [the documentation](https://developer.mastercard.com/unified-checkout-solutions/documentation/sdk-reference/init).
 *
 * @property disableSummaryScreen Boolean flag that controls if a final summary screen is presented in the checkout flow.
 *                               If true, the summary screen is disabled; if false or null, the summary screen is shown.
 * @property dpaData Object where the DPA (Digital Payment Application) creation data is stored. This includes information
 *                   necessary for the initialization and execution of the digital payment process.
 * @property cardBrands List of allowed card brands - options: 'mastercard', 'maestro', 'visa', 'amex', 'discover'.
 *                      This list defines which card brands are permitted for transactions within the checkout flow.
 * @property coBrandNames List of co-brand names associated with the SRC (Secure Remote Commerce) experience. Co-brands
 *                        might include additional card associations or partnerships.
 * @property checkoutExperience Checkout experience type, either 'WITHIN_CHECKOUT' or 'PAYMENT_SETTINGS'.
 *                              'WITHIN_CHECKOUT' indicates the experience is embedded within the checkout process,
 *                              while 'PAYMENT_SETTINGS' suggests a more standalone payment settings interface.
 * @property services Services offered, such as 'INLINE_CHECKOUT' or 'INLINE_INSTALLMENTS'. These services define the
 *                    additional capabilities or options available during the checkout process.
 * @property dpaTransactionOptions Object that stores options for creating a transaction with DPA. These options might
 *                                 include specific transaction parameters or preferences.
 * @property customer Customer-specific data used during the checkout process. This may include information such as
 *                    customer ID, preferences, or other relevant details.
 * @property unacceptedCardType Type of card that is not accepted in the checkout flow. This can be used to explicitly
 *                              define cards that should be rejected or flagged during the transaction process.
 */
@Serializable
data class ClickToPayMeta(
    @SerialName("dpa_data") override val dpaData: ClickToPayDPAData? = null,
    @SerialName("disable_summary_screen") val disableSummaryScreen: Boolean? = null,
    @SerialName("card_brands") val cardBrands: List<CardBrands>? = null,
    @SerialName("co_brand_names") val coBrandNames: List<String>? = null,
    @SerialName("checkout_experience") val checkoutExperience: CheckoutExperience? = null,
    val services: Services? = null,
    @SerialName("dpa_transaction_options") override val dpaTransactionOptions: ClickToPayDPAOptions? = null,
    val customer: Customer? = null,
    @SerialName("unaccepted_card_type") val unacceptedCardType: UnacceptedCardType? = null
) : BaseSRCMeta(dpaData = dpaData, dpaTransactionOptions = dpaTransactionOptions)
