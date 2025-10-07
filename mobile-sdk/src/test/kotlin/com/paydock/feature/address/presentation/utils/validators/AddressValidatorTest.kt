package com.paydock.feature.address.presentation.utils.validators

import com.paydock.feature.address.utils.errors.AddressInputError
import com.paydock.feature.address.utils.validators.AddressValidator
import org.junit.Test
import kotlin.test.assertEquals

class AddressValidatorTest {

    @Test
    fun `validateInput with blank value and user interaction returns Empty error`() {
        // Arrange
        val inputValue = "   " // Blank string with whitespace
        val userInteracted = true

        // Act
        val result = AddressValidator.validateInput(inputValue, userInteracted)

        // Assert
        assertEquals(AddressInputError.Empty, result)
    }

    @Test
    fun `validateInput with empty value and user interaction returns Empty error`() {
        // Arrange
        val inputValue = "" // Empty string
        val userInteracted = true

        // Act
        val result = AddressValidator.validateInput(inputValue, userInteracted)

        // Assert
        assertEquals(AddressInputError.Empty, result)
    }

    @Test
    fun `validateInput with blank value and NO user interaction returns None error`() {
        // Arrange
        val inputValue = "   "
        val userInteracted = false

        // Act
        val result = AddressValidator.validateInput(inputValue, userInteracted)

        // Assert
        assertEquals(AddressInputError.None, result)
    }

    @Test
    fun `validateInput with empty value and NO user interaction returns None error`() {
        // Arrange
        val inputValue = ""
        val userInteracted = false

        // Act
        val result = AddressValidator.validateInput(inputValue, userInteracted)

        // Assert
        assertEquals(AddressInputError.None, result)
    }

    @Test
    fun `validateInput with non-blank value and user interaction returns None error`() {
        // Arrange
        val inputValue = "123 Main St"
        val userInteracted = true

        // Act
        val result = AddressValidator.validateInput(inputValue, userInteracted)

        // Assert
        assertEquals(AddressInputError.None, result)
    }

    @Test
    fun `validateInput with non-blank value (with leading and trailing spaces) and user interaction returns None error`() {
        // Arrange
        val inputValue = "  Sydney  "
        val userInteracted = true

        // Act
        val result = AddressValidator.validateInput(inputValue, userInteracted)

        // Assert
        assertEquals(AddressInputError.None, result)
    }

    @Test
    fun `validateInput with non-blank value and NO user interaction returns None error`() {
        // Arrange
        val inputValue = "New York"
        val userInteracted = false

        // Act
        val result = AddressValidator.validateInput(inputValue, userInteracted)

        // Assert
        assertEquals(AddressInputError.None, result)
    }
}
