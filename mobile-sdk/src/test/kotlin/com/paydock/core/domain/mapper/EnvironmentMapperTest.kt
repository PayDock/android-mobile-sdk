package com.paydock.core.domain.mapper

import com.afterpay.android.AfterpayEnvironment
import com.paydock.core.BaseUnitTest
import com.paydock.core.ClientSDKConstants
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.model.Environment
import kotlin.test.Test
import kotlin.test.assertEquals
import com.paypal.android.corepayments.Environment as PayPalEnvironment

class EnvironmentMapperTest : BaseUnitTest() {

    @Test
    fun mapToBaseUrl_production_returnsProductionBaseUrl() {
        val environment = Environment.PRODUCTION
        val expectedBaseUrl = MobileSDKConstants.BaseUrls.PRODUCTION_BASE_URL
        val actualBaseUrl = environment.mapToBaseUrl()
        assertEquals(expectedBaseUrl, actualBaseUrl)
    }

    @Test
    fun mapToBaseUrl_sandbox_returnsSandboxBaseUrl() {
        val environment = Environment.SANDBOX
        val expectedBaseUrl = MobileSDKConstants.BaseUrls.SANDBOX_BASE_URL
        val actualBaseUrl = environment.mapToBaseUrl()
        assertEquals(expectedBaseUrl, actualBaseUrl)
    }

    @Test
    fun mapToBaseUrl_staging_returnsStagingBaseUrl() {
        val environment = Environment.STAGING
        val expectedBaseUrl = MobileSDKConstants.BaseUrls.STAGING_BASE_URL
        val actualBaseUrl = environment.mapToBaseUrl()
        assertEquals(expectedBaseUrl, actualBaseUrl)
    }

    @Test
    fun mapToClientSDKLibrary_production_returnsProductionLibrary() {
        val environment = Environment.PRODUCTION
        val expectedLibrary = ClientSDKConstants.Library.PROD
        val actualLibrary = environment.mapToClientSDKLibrary()
        assertEquals(expectedLibrary, actualLibrary)
    }

    @Test
    fun mapToClientSDKLibrary_sandbox_returnsStagingSandboxLibrary() {
        val environment = Environment.SANDBOX
        val expectedLibrary = ClientSDKConstants.Library.STAGING_SANDBOX
        val actualLibrary = environment.mapToClientSDKLibrary()
        assertEquals(expectedLibrary, actualLibrary)
    }

    @Test
    fun mapToClientSDKLibrary_staging_returnsStagingSandboxLibrary() {
        val environment = Environment.STAGING
        val expectedLibrary = ClientSDKConstants.Library.STAGING_SANDBOX
        val actualLibrary = environment.mapToClientSDKLibrary()
        assertEquals(expectedLibrary, actualLibrary)
    }

    @Test
    fun mapToClientSDKEnv_production_returnsProductionEnv() {
        val environment = Environment.PRODUCTION
        val expectedEnv = ClientSDKConstants.Env.PROD
        val actualEnv = environment.mapToClientSDKEnv()
        assertEquals(expectedEnv, actualEnv)
    }

    @Test
    fun mapToClientSDKEnv_sandbox_returnsSandboxEnv() {
        val environment = Environment.SANDBOX
        val expectedEnv = ClientSDKConstants.Env.SANDBOX
        val actualEnv = environment.mapToClientSDKEnv()
        assertEquals(expectedEnv, actualEnv)
    }

    @Test
    fun mapToClientSDKEnv_staging_returnsStagingEnv() {
        val environment = Environment.STAGING
        val expectedEnv = ClientSDKConstants.Env.STAGING
        val actualEnv = environment.mapToClientSDKEnv()
        assertEquals(expectedEnv, actualEnv)
    }

    @Test
    fun mapToPayPalEnv_production_returnsLiveEnv() {
        val environment = Environment.PRODUCTION
        val expectedEnv = PayPalEnvironment.LIVE
        val actualEnv = environment.mapToPayPalEnv()
        assertEquals(expectedEnv, actualEnv)
    }

    @Test
    fun mapToPayPalEnv_sandbox_returnsSandboxEnv() {
        val environment = Environment.SANDBOX
        val expectedEnv = PayPalEnvironment.SANDBOX
        val actualEnv = environment.mapToPayPalEnv()
        assertEquals(expectedEnv, actualEnv)
    }

    @Test
    fun mapToPayPalEnv_staging_returnsSandboxEnv() {
        val environment = Environment.STAGING
        val expectedEnv = PayPalEnvironment.SANDBOX
        val actualEnv = environment.mapToPayPalEnv()
        assertEquals(expectedEnv, actualEnv)
    }

    @Test
    fun mapToAfterpayEnv_production_returnsProductionEnv() {
        val environment = Environment.PRODUCTION
        val expectedEnv = AfterpayEnvironment.PRODUCTION
        val actualEnv = environment.mapToAfterpayEnv()
        assertEquals(expectedEnv, actualEnv)
    }

    @Test
    fun mapToAfterpayEnv_sandbox_returnsSandboxEnv() {
        val environment = Environment.SANDBOX
        val expectedEnv = AfterpayEnvironment.SANDBOX
        val actualEnv = environment.mapToAfterpayEnv()
        assertEquals(expectedEnv, actualEnv)
    }

    @Test
    fun mapToAfterpayEnv_staging_returnsSandboxEnv() {
        val environment = Environment.STAGING
        val expectedEnv = AfterpayEnvironment.SANDBOX
        val actualEnv = environment.mapToAfterpayEnv()
        assertEquals(expectedEnv, actualEnv)
    }

    @Test
    fun mapToFlyPayEnv_production_returnsProductionUrl() {
        val environment = Environment.PRODUCTION
        val flyPayOrderId = "testOrderId"
        val clientId = "testClientId"
        val expectedUrl = "https://checkout.flypay.com.au/?" +
            "orderId=$flyPayOrderId&" +
            "redirectUrl=${MobileSDKConstants.FlyPayConfig.FLY_PAY_REDIRECT_URL}&" +
            "mode=default&" +
            "clientId=$clientId"
        val actualUrl = environment.mapToFlyPayEnv(flyPayOrderId, clientId)
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun mapToFlyPayEnv_sandbox_returnsSandboxUrl() {
        val environment = Environment.SANDBOX
        val flyPayOrderId = "testOrderId"
        val clientId = "testClientId"
        val expectedUrl = "https://checkout.sandbox.cxbflypay.com.au/?" +
            "orderId=$flyPayOrderId&" +
            "redirectUrl=${MobileSDKConstants.FlyPayConfig.FLY_PAY_REDIRECT_URL}&" +
            "mode=default&" +
            "clientId=$clientId"
        val actualUrl = environment.mapToFlyPayEnv(flyPayOrderId, clientId)
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun mapToFlyPayEnv_staging_returnsSandboxUrl() {
        val environment = Environment.STAGING
        val flyPayOrderId = "testOrderId"
        val clientId = "testClientId"
        val expectedUrl = "https://checkout.sandbox.cxbflypay.com.au/?" +
            "orderId=$flyPayOrderId&" +
            "redirectUrl=${MobileSDKConstants.FlyPayConfig.FLY_PAY_REDIRECT_URL}&" +
            "mode=default&" +
            "clientId=$clientId"
        val actualUrl = environment.mapToFlyPayEnv(flyPayOrderId, clientId)
        assertEquals(expectedUrl, actualUrl)
    }
}