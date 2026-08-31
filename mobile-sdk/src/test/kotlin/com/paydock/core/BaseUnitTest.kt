package com.paydock.core

import android.net.Uri
import android.util.Log
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.koin.test.KoinTest
import org.koin.test.mock.MockProviderRule
import org.mockito.Mockito
import sun.misc.Unsafe
import java.lang.reflect.Field
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

        // Used for JWT Helper (Token parsing)
        mockkStatic(android.util.Base64::class)
        val b64Encoded = slot<String>()
        val flags = slot<Int>()
        every { android.util.Base64.decode(capture(b64Encoded), capture(flags)) } coAnswers {
            java.util.Base64.getDecoder().decode(this.args[0] as String)
        }
    }

    @After
    fun tearDownMocks() {
        clearAllMocks()
    }

    fun readResourceFile(path: String): String {
        val file = "src/test/resources/$path"
        return String(Files.readAllBytes(Paths.get(file)))
    }

    /**
     * Overwrites a `static final` field for test purposes (e.g. `Build.VERSION.SDK_INT`).
     *
     * On JDK 9+, the classic trick of clearing the FINAL bit on `Field`'s own `modifiers`
     * bookkeeping field via reflection no longer reliably works: that bookkeeping field is
     * filtered from reflective access on modern JDKs, and even where it can still be located,
     * the JVM's `Field`/`MethodHandle` accessors (e.g. `jdk.internal.reflect.MethodHandleAccessorFactory`
     * on JDK 17+) enforce the *actual* static-final-ness of the field independently of that
     * bookkeeping value. The result is `field.set(null, value)` throwing
     * `IllegalAccessException: static final field has no write access ... putStatic`
     * (wrapped in an `InternalError`). No `--add-opens` flag fixes this: it isn't a module
     * access restriction, it's `MethodHandles.Lookup` refusing to hand out a setter for a
     * `static final` field belonging to another class, by design (JLS/JVM spec).
     *
     * `sun.misc.Unsafe` writes directly to the field's memory location instead of going through
     * `Field`/`MethodHandle` accessors, so it is unaffected by that final-field check and works
     * consistently across JDK versions.
     */
    fun setStaticFieldViaReflection(field: Field, value: Any) {
        field.isAccessible = true
        val unsafe = unsafeInstance()
        val base = unsafe.staticFieldBase(field)
        val offset = unsafe.staticFieldOffset(field)
        when (value) {
            is Boolean -> unsafe.putBoolean(base, offset, value)
            is Byte -> unsafe.putByte(base, offset, value)
            is Char -> unsafe.putChar(base, offset, value)
            is Short -> unsafe.putShort(base, offset, value)
            is Int -> unsafe.putInt(base, offset, value)
            is Long -> unsafe.putLong(base, offset, value)
            is Float -> unsafe.putFloat(base, offset, value)
            is Double -> unsafe.putDouble(base, offset, value)
            else -> unsafe.putObject(base, offset, value)
        }
    }

    private fun unsafeInstance(): Unsafe {
        val theUnsafeField: Field = Unsafe::class.java.getDeclaredField("theUnsafe")
        theUnsafeField.isAccessible = true
        return theUnsafeField.get(null) as Unsafe
    }
}