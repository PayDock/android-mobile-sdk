package com.paydock.core

import android.net.Uri
import android.util.Log
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.koin.test.KoinTest
import org.koin.test.mock.MockProviderRule
import org.mockito.Mockito
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Paths

abstract class BaseUnitTest : KoinTest {

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        Mockito.mock(clazz.java)
    }

    @Before
    open fun setUpMocks() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        mockkStatic(Uri::class)
    }

    @After
    fun tearDownMocks() {
        clearAllMocks()
    }

    fun readResourceFile(path: String): String {
        val file = "src/test/resources/$path"
        return String(Files.readAllBytes(Paths.get(file)))
    }

    fun setStaticFieldViaReflection(field: Field, value: Any) {
        field.isAccessible = true
        getModifiersField().also {
            it.isAccessible = true
            it.set(field, field.modifiers and Modifier.FINAL.inv())
        }
        field.set(null, value)
    }

    @Suppress("NestedBlockDepth")
    private fun getModifiersField(): Field {
        return try {
            Field::class.java.getDeclaredField("modifiers")
        } catch (e: NoSuchFieldException) {
            try {
                val getDeclaredFields0: Method =
                    Class::class.java.getDeclaredMethod(
                        "getDeclaredFields0",
                        Boolean::class.javaPrimitiveType
                    )
                getDeclaredFields0.isAccessible = true
                val fields = getDeclaredFields0.invoke(Field::class.java, false) as Array<Field>
                for (field in fields) {
                    if ("modifiers" == field.name) {
                        return field
                    }
                }
            } catch (ex: ReflectiveOperationException) {
                e.addSuppressed(ex)
            }
            throw e
        }
    }
}