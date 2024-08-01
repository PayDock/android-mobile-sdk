package com.paydock.core.utils.jwt

import android.os.Build
import com.paydock.core.BaseUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.utils.jwt.models.MetaTokenPayload
import com.paydock.core.utils.jwt.models.WalletTokenPayload
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class JwtHelperTest : BaseUnitTest() {

    override fun setUpMocks() {
        super.setUpMocks()
        mockkStatic(android.util.Base64::class)
        val b64Encoded = slot<String>()
        val flags = slot<Int>()
        every { android.util.Base64.decode(capture(b64Encoded), capture(flags)) } coAnswers {
            java.util.Base64.getDecoder().decode(this.args[0] as String)
        }
    }

    @Test
    fun `test getWalletTokenPayload for SDK_INT = 33 returns expected parsed wallet token payload`() {
        // Arrange
        setStaticFieldViaReflection(
            Build.VERSION::class.java.getDeclaredField("SDK_INT"),
            Build.VERSION_CODES.TIRAMISU
        )
        setStaticFieldViaReflection(
            Build.VERSION::class.java.getDeclaredField("SDK_INT"),
            Build.VERSION_CODES.TIRAMISU
        )
        val walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val expectedPayloadJson = MobileSDKTestConstants.Jwt.MOCK_WALLET_TOKEN_PAYLOAD
        val expectedPayload = expectedPayloadJson.convertToDataClass<WalletTokenPayload>()

        // Act
        val result = JwtHelper.getWalletTokenPayload(walletToken)

        // Assert
        assertNotNull(result)
        assertEquals(expectedPayload, result)
    }

    @Test
    fun `test getWalletTokenPayload for SDK_INT = 24 returns expected parsed wallet token payload`() {
        // Arrange
        setStaticFieldViaReflection(
            Build.VERSION::class.java.getDeclaredField("SDK_INT"),
            Build.VERSION_CODES.N
        )

        val walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val expectedPayloadJson = MobileSDKTestConstants.Jwt.MOCK_WALLET_TOKEN_PAYLOAD
        val expectedPayload = expectedPayloadJson.convertToDataClass<WalletTokenPayload>()

        // Act
        val result = JwtHelper.getWalletTokenPayload(walletToken)

        // Assert
        assertNotNull(result)
        assertEquals(expectedPayload, result)
    }

    @Test
    fun `test getMetaTokenPayload returns expected parsed meta token payload`() {
        // Arrange
        val walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val expectedPayloadJson = MobileSDKTestConstants.Jwt.MOCK_META_TOKEN_PAYLOAD
        val expectedPayload = expectedPayloadJson.convertToDataClass<MetaTokenPayload>()

        // Act
        val result = JwtHelper.getMetaTokenPayload(walletToken)

        // Assert
        assertNotNull(result)
        assertEquals(expectedPayload, result)
    }

    @Test
    fun `test getChargeIdToken returns expected parsed chargeId`() {
        // Arrange
        val walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val expectedChargeId = MobileSDKTestConstants.Charge.MOCK_CHARGE_ID

        // Act
        val result = JwtHelper.getChargeIdToken(walletToken)

        // Assert
        assertNotNull(result)
        assertEquals(expectedChargeId, result)
    }

    @Test
    fun `test getTokenExpirationDate returns expected parsed expiration date`() {
        // Arrange
        val walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val dateString = MobileSDKTestConstants.Jwt.MOCK_EXPIRATION_DATE_STRING
        val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val expectedDate = format.parse(dateString)

        // Act
        val result = JwtHelper.getTokenExpirationDate(walletToken)

        // Assert
        assertNotNull(result)
        assertEquals(expectedDate, result)
    }

    @Test
    fun `test isTokenExpired returns false`() {
        // Arrange
        val walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN

        // Act
        val result = JwtHelper.isTokenExpired(walletToken)

        // Assert
        assertNotNull(result)
        assertTrue(result)
    }
}