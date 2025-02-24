package com.paydock.feature.threeDS.common.utils

import com.paydock.core.BaseUnitTest
import com.paydock.feature.threeDS.common.domain.model.ui.Token
import com.paydock.feature.threeDS.common.domain.model.ui.enums.TokenFormat
import com.paydock.feature.threeDS.common.domain.presentation.utils.ThreeDSTokenUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Suppress("MaxLineLength")
class ThreeDSTokenUtilsTest : BaseUnitTest() {

    @Test
    fun `extractToken should return Token object for valid integrated 3DS token`() {
        // Arrange
        val token = Token(
            content = "<div id=\"threedsChallengeRedirect\" xmlns=\"http://www.w3.org/1999/html\" style=\" height: 100vh\"> <form id =\"threedsChallengeRedirectForm\" method=\"POST\" action=\"https://mtf.gateway.mastercard.com/acs/mastercard/v2/prompt\" target=\"challengeFrame\"> <input type=\"hidden\" name=\"creq\" value=\"eyJ0aHJlZURTU2VydmVyVHJhbnNJRCI6ImIzYWRiODk4LTA4YmYtNDA3YS04ZTRiLTM3ODlmOTY1ZGZmYiJ9\" /> </form> <iframe id=\"challengeFrame\" name=\"challengeFrame\" width=\"100%\" height=\"100%\" ></iframe> <script id=\"authenticate-payer-script\"> var e=document.getElementById(\"threedsChallengeRedirectForm\"); if (e) { e.submit(); if (e.parentNode !== null) { e.parentNode.removeChild(e); } } </script> </div>",
            format = TokenFormat.HTML,
            charge3dsId = "262e0844-066b-4545-93dd-bd3ffbcefe8b"
        )
        // Integrated 3DS Token
        val encodedToken =
            "eyJjb250ZW50IjoiPGRpdiBpZD1cInRocmVlZHNDaGFsbGVuZ2VSZWRpcmVjdFwiIHhtbG5zPVwiaHR0cDovL3d3dy53My5vcmcvMTk5OS9odG1sXCIgc3R5bGU9XCIgaGVpZ2h0OiAxMDB2aFwiPiA8Zm9ybSBpZCA9XCJ0aHJlZWRzQ2hhbGxlbmdlUmVkaXJlY3RGb3JtXCIgbWV0aG9kPVwiUE9TVFwiIGFjdGlvbj1cImh0dHBzOi8vbXRmLmdhdGV3YXkubWFzdGVyY2FyZC5jb20vYWNzL21hc3RlcmNhcmQvdjIvcHJvbXB0XCIgdGFyZ2V0PVwiY2hhbGxlbmdlRnJhbWVcIj4gPGlucHV0IHR5cGU9XCJoaWRkZW5cIiBuYW1lPVwiY3JlcVwiIHZhbHVlPVwiZXlKMGFISmxaVVJUVTJWeWRtVnlWSEpoYm5OSlJDSTZJbUl6WVdSaU9EazRMVEE0WW1ZdE5EQTNZUzA0WlRSaUxUTTNPRGxtT1RZMVpHWm1ZaUo5XCIgLz4gPC9mb3JtPiA8aWZyYW1lIGlkPVwiY2hhbGxlbmdlRnJhbWVcIiBuYW1lPVwiY2hhbGxlbmdlRnJhbWVcIiB3aWR0aD1cIjEwMCVcIiBoZWlnaHQ9XCIxMDAlXCIgPjwvaWZyYW1lPiA8c2NyaXB0IGlkPVwiYXV0aGVudGljYXRlLXBheWVyLXNjcmlwdFwiPiB2YXIgZT1kb2N1bWVudC5nZXRFbGVtZW50QnlJZChcInRocmVlZHNDaGFsbGVuZ2VSZWRpcmVjdEZvcm1cIik7IGlmIChlKSB7IGUuc3VibWl0KCk7IGlmIChlLnBhcmVudE5vZGUgIT09IG51bGwpIHsgZS5wYXJlbnROb2RlLnJlbW92ZUNoaWxkKGUpOyB9IH0gPC9zY3JpcHQ+IDwvZGl2PiIsImZvcm1hdCI6Imh0bWwiLCJjaGFyZ2VfM2RzX2lkIjoiMjYyZTA4NDQtMDY2Yi00NTQ1LTkzZGQtYmQzZmZiY2VmZThiIn0="

        // Act
        val result = ThreeDSTokenUtils.extractToken(encodedToken)

        // Assert
        assertEquals(token, result)
    }

    @Test
    fun `extractToken should return Token object for valid standalone 3DS token`() {
        // Arrange
        val token = Token(
            content = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY3YjZmOTAyNjIyZTUwMGQ1N2YyMjI1ZSIsIm1ldGEiOiJleUpqYUdGeVoyVmZNMlJ6WDJsa0lqb2lNek0xTW1JeVpqUXRaVGN3TUMwME1HSTBMVGxsWm1ZdFkyRTROamsxTW1JNU1UaGtJaXdpYzJWeWRtbGpaVjkwZVhCbElqb2lSMUJoZVcxbGJuUnpJaXdpWlhoMFpYSnVZV3hmYVdRaU9pSTNZak5sTjJZME9TMDNaRGhtTFRReU0yWXRPR0U1WlMxa1pqaGtObUl5WkdJM016Y2lMQ0pwYm1sMGFXRnNhWHBoZEdsdmJsOTFjbXdpT2lKb2RIUndjem92TDNCaGVXUnZZMnN0ZEdWemRDNWhjekV1WjNCaGVXMWxiblJ6TG01bGRDOWhjR2t2ZGpJdlluSjNMMk5oYkd4aVlXTnJQM1J5WVc1elNXUTlOMkl6WlRkbU5Ea3ROMlE0WmkwME1qTm1MVGhoT1dVdFpHWTRaRFppTW1SaU56TTNKblE5Um00eU4zSjFUVmxqT1d0Q09FcFJZV3hCUTJKRlNHMUNlR2RTYW5KV1JGbE1hRXg0YlhGc1NVSkRNV2x0TmxKWmJIaElXSGQzVDFwalZVZzBXRnBVVWxremRXNDFWbmR3SWl3aVlYVjBhRzl5YVhwaGRHbHZibDkxY213aU9pSm9kSFJ3Y3pvdkwzQmhlV1J2WTJzdGRHVnpkQzVoY0drdVlYTXhMbWR3WVhsdFpXNTBjeTV1WlhRdllYQnBMM1l5TDJGMWRHZ3ZZbkozUDNROVpHWXlNekJrTm1Gak5tSTJPV1ExWkRNME9HUTROVEUwTWpJellqQmlNVEVtY0QxT2VsSnRXV3BGTWs1VVdYcGFiVlV5VFZkSk5GcFhXWGhPTWxreFdWUm5NMWxVYUd4TlJFMHpXa1JaZWxsNldURlBWRTB3VFhjaUxDSnpaV052Ym1SaGNubGZkWEpzSWpvaWFIUjBjSE02THk5d1lYbGtiMk5yTFhSbGMzUXVZWE14TG1kd1lYbHRaVzUwY3k1dVpYUXZZWEJwTDNZeUwySnlkeTlwYm1sMEwyMXZiajkwUFRkaU0yVTNaalE1TFRka09HWXROREl6WmkwNFlUbGxMV1JtT0dRMllqSmtZamN6TnlKOSIsImlhdCI6MTc0MDA0NDU0NiwiZXhwIjoxNzQwMTMwOTQ2fQ.xmXXiKvK0KrwRVpSFw-W_bFomnBLhQTtpTgPSy54FsE",
            format = TokenFormat.STANDALONE_3DS,
            charge3dsId = null
        )

        // Integrated 3DS Token
        val encodedToken =
            "eyJjb250ZW50IjoiZXlKaGJHY2lPaUpJVXpJMU5pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SnBaQ0k2SWpZM1lqWm1PVEF5TmpJeVpUVXdNR1ExTjJZeU1qSTFaU0lzSW0xbGRHRWlPaUpsZVVwcVlVZEdlVm95Vm1aTk1sSjZXREpzYTBscWIybE5lazB4VFcxSmVWcHFVWFJhVkdOM1RVTXdNRTFIU1RCTVZHeHNXbTFaZEZreVJUUk9hbXN4VFcxSk5VMVVhR3RKYVhkcFl6SldlV1J0YkdwYVZqa3daVmhDYkVscWIybFNNVUpvWlZjeGJHSnVVbnBKYVhkcFdsaG9NRnBZU25WWlYzaG1ZVmRSYVU5cFNUTlphazVzVGpKWk1FOVRNRE5hUkdodFRGUlJlVTB5V1hSUFIwVTFXbE14YTFwcWFHdE9iVWw1V2tkSk0wMTZZMmxNUTBwd1ltMXNNR0ZYUm5OaFdIQm9aRWRzZG1Kc09URmpiWGRwVDJsS2IyUklVbmRqZW05MlRETkNhR1ZYVW5aWk1uTjBaRWRXZW1SRE5XaGpla1YxV2pOQ2FHVlhNV3hpYmxKNlRHMDFiR1JET1doalIydDJaR3BKZGxsdVNqTk1NazVvWWtkNGFWbFhUbkpRTTFKNVdWYzFlbE5YVVRsT01rbDZXbFJrYlU1RWEzUk9NbEUwV21rd01FMXFUbTFNVkdob1QxZFZkRnBIV1RSYVJGcHBUVzFTYVU1NlRUTktibEU1VW0wMGVVNHpTakZVVm14cVQxZDBRMDlGY0ZKWlYzaENVVEpLUmxOSE1VTmxSMlJUWVc1S1YxSkdiRTFoUlhnMFlsaEdjMU5WU2tSTlYyeDBUbXhLV21KSWFFbFhTR1F6VkRGd2FsWlZaekJYUm5CVlZXeHJlbVJYTkRGV2JtUjNTV2wzYVZsWVZqQmhSemw1WVZod2FHUkhiSFppYkRreFkyMTNhVTlwU205a1NGSjNZM3B2ZGt3elFtaGxWMUoyV1RKemRHUkhWbnBrUXpWb1kwZHJkVmxZVFhoTWJXUjNXVmhzZEZwWE5UQmplVFYxV2xoUmRsbFlRbkJNTTFsNVRESkdNV1JIWjNaWmJrb3pVRE5ST1ZwSFdYbE5la0pyVG0xR2FrNXRTVEpQVjFFeFdrUk5NRTlIVVRST1ZFVXdUV3BKZWxscVFtbE5WRVZ0WTBReFQyVnNTblJYVjNCR1RXczFWVmRZY0dGaVZsVjVWRlprU2s1R2NGaFhXR2hQVFd4cmVGZFdVbTVOTVd4VllVZDRUbEpGTUhwWGExSmFaV3hzTmxkVVJsQldSVEIzVkZoamFVeERTbnBhVjA1MlltMVNhR051Ykdaa1dFcHpTV3B2YVdGSVVqQmpTRTAyVEhrNWQxbFliR3RpTWs1eVRGaFNiR016VVhWWldFMTRURzFrZDFsWWJIUmFWelV3WTNrMWRWcFlVWFpaV0VKd1RETlplVXd5U25sa2VUbHdZbTFzTUV3eU1YWmlhamt3VUZSa2FVMHlWVE5hYWxFMVRGUmthMDlIV1hST1JFbDZXbWt3TkZsVWJHeE1WMUp0VDBkUk1sbHFTbXRaYW1ONlRubEtPU0lzSW1saGRDSTZNVGMwTURBME5EVTBOaXdpWlhod0lqb3hOelF3TVRNd09UUTJmUS54bVhYaUt2SzBLcndSVnBTRnctV19iRm9tbkJMaFFUdHBUZ1BTeTU0RnNFIiwiZm9ybWF0Ijoic3RhbmRhbG9uZV8zZHMifQ=="

        // Act
        val result = ThreeDSTokenUtils.extractToken(encodedToken)

        // Assert
        assertEquals(token, result)
    }

    @Test
    fun `extractToken should return null for invalid base64 encoded token`() {
        // Arrange
        val invalidEncodedToken = "invalid-base64"

        // Act
        val result = ThreeDSTokenUtils.extractToken(invalidEncodedToken)

        // Assert
        assertNull(result)
    }

    @Test
    fun `extractToken should return null for empty encoded token`() {
        // Arrange
        val emptyEncodedToken = ""

        // Act
        val result = ThreeDSTokenUtils.extractToken(emptyEncodedToken)

        // Assert
        assertNull(result)
    }
}