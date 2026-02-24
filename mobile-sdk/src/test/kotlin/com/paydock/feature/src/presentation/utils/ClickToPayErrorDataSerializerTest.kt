package com.paydock.feature.src.presentation.utils

import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.src.domain.model.ui.ErrorData
import com.paydock.feature.src.domain.model.ui.enums.EventDataType
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Suppress("MaxLineLength")
internal class ClickToPayErrorDataSerializerTest {

    @Test
    fun `test error data serializer with UserErrorData`() {
        // Arrange
        val eventJson =
            "{\"type\":\"UserError\",\"data\":\"Request failed due to CARD_MISSING\"}"

        // Act
        val result = eventJson.convertToDataClass<ErrorData>()

        // Assert
        assertIs<ErrorData.UserErrorData>(result)
        assertEquals(EventDataType.USER_ERROR, result.type)
        assertTrue(result.data.isNotEmpty())
        assertEquals("Request failed due to CARD_MISSING", result.data)
    }

    @Test
    fun `test error data serializer with UserErrorData array format`() {
        // Arrange - Web SDK sends data as array of error objects
        val eventJson =
            """{"type":"UserError","data":[{"field":"unknown","message":"Client specified an invalid argument. Check error message and error details for more information.","originalError":{"reason":"INVALID_PARAMETER","message":"Client specified an invalid argument. Check error message and error details for more information.","status":400,"sourceType":"CARD_ENROLLMENT"}}]}"""

        // Act
        val result = eventJson.convertToDataClass<ErrorData>()

        // Assert
        assertIs<ErrorData.UserErrorData>(result)
        assertEquals(EventDataType.USER_ERROR, result.type)
        assertEquals(
            "Client specified an invalid argument. Check error message and error details for more information.",
            result.data
        )
    }

    @Test
    fun `test error data serializer with CriticalErrorData`() {
        // Arrange
        val eventJson =
            "{\"type\":\"CriticalError\",\"data\":\"403 - OK [object Object]\"}"

        // Act
        val result = eventJson.convertToDataClass<ErrorData>()

        // Assert
        assertIs<ErrorData.CriticalErrorData>(result)
        assertEquals(EventDataType.CRITICAL_ERROR, result.type)
        assertTrue(result.data.isNotEmpty())
    }

    @Test(expected = SerializationException::class)
    fun `test error data serializer with unknown error serializer`() {
        // Arrange
        val eventJson =
            "{\"type\":\"UnknownError\",\"data\":\"403 - OK [object Object]\"}"

        eventJson.convertToDataClass<ErrorData>()
    }
}
