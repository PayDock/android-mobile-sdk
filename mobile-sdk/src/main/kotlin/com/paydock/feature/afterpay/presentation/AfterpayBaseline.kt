package com.paydock.feature.afterpay.presentation

import com.afterpay.android.Afterpay
import com.paydock.MobileSDK
import com.paydock.core.domain.mapper.mapToAfterpayEnv
import java.util.Currency
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Applies a baseline Afterpay SDK configuration before any Afterpay UI is constructed.
 *
 * **Why this is required (Afterpay 4.8.x):**
 * `AfterpayPaymentButton.ButtonText` resolves its brand artwork — the classic Afterpay
 * wordmark vs. the en-US Cash App Afterpay lockup — once, inside its static initializer, by
 * reading `Afterpay.getDrawables()`. That lookup is keyed off the *currently configured*
 * locale; when no configuration has been applied yet, Afterpay falls back to `en-US`, which
 * maps to the Cash App lockup. The resolved drawable is then cached on the enum value for the
 * lifetime of the process, so a later `setConfiguration` call can never change the button's
 * wordmark.
 *
 * The per-widget configuration in [com.paydock.feature.afterpay.presentation.viewmodels.AfterpayViewModel]
 * runs asynchronously and therefore lands *after* the enum has already been referenced (e.g. by
 * [AfterpayAppearanceDefaults.appearance]), so it is too late to influence the brand artwork.
 *
 * Applying a baseline configuration here — synchronously and before the enum is referenced —
 * ensures the button's brand artwork is resolved from the intended locale. The locale is
 * supplied by [MobileSDK] at initialisation; when none is provided it falls back to
 * [DEFAULT_LOCALE] (the device locale), so the branding follows the device's region
 * (classic Afterpay in AU/UK/NZ/CA, Cash App Afterpay in the US) unless explicitly overridden.
 *
 * Note: because the brand artwork is captured on first enum load, only the *first* call to
 * [ensureConfigured] influences the branding for the lifetime of the process; later calls are
 * no-ops. [MobileSDK.initialize] calls this before any UI is built, so the locale passed there
 * governs the branding.
 */
internal object AfterpayBaseline {

    /**
     * Fallback locale used when no explicit locale is supplied to [ensureConfigured]: the current
     * device locale ([Locale.getDefault]). This makes the payment button's branding follow the
     * device's region by default.
     */
    val DEFAULT_LOCALE: Locale get() = Locale.getDefault()

    private val applied = AtomicBoolean(false)

    /**
     * Applies the baseline configuration exactly once per process. Idempotent and safe to call
     * repeatedly; subsequent calls are no-ops. Failures (e.g. SDK not yet initialised, or a
     * locale without a resolvable currency) are swallowed so they never break rendering — a later
     * call will retry.
     *
     * @param locale Locale used to resolve the button's branding/currency. When `null`, falls back
     *   to [DEFAULT_LOCALE] (the device locale). Only the first successful call takes effect
     *   (see class docs).
     */
    fun ensureConfigured(locale: Locale? = null) {
        if (!applied.compareAndSet(false, true)) return
        val effectiveLocale = locale ?: DEFAULT_LOCALE
        runCatching {
            Afterpay.setConfiguration(
                minimumAmount = "1",
                maximumAmount = "1000",
                currencyCode = Currency.getInstance(effectiveLocale).currencyCode,
                locale = Locale.Builder()
                    .setLanguage(effectiveLocale.language)
                    .setRegion(effectiveLocale.country)
                    .build(),
                environment = MobileSDK.getInstance().environment.mapToAfterpayEnv(),
                consumerLocale = effectiveLocale
            )
        }.onFailure {
            // Allow a later attempt if configuration could not be applied yet.
            applied.set(false)
        }
    }
}
