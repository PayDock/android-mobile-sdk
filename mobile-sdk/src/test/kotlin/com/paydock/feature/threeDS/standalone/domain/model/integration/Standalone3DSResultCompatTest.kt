package com.paydock.feature.threeDS.standalone.domain.model.integration

import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.Standalone3DSAuthenticationFlow
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.StandaloneEventType
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.jvm.internal.DefaultConstructorMarker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pins the binary-compatibility bridges of [Standalone3DSResult]: apps compiled against SDK 5.7.x (and earlier) link
 * against these JVM signatures, so each must exist, keep `authenticationFlow` as it was and stay hidden from Java.
 */
class Standalone3DSResultCompatTest {

    private val type = Standalone3DSResult::class.java
    private val eventType = StandaloneEventType::class.java
    private val string = String::class.java
    private val int = Int::class.javaPrimitiveType!!
    private val marker = DefaultConstructorMarker::class.java

    private fun constructor(vararg parameters: Class<*>): Constructor<Standalone3DSResult> =
        type.getDeclaredConstructor(*parameters)

    private fun method(name: String, vararg parameters: Class<*>): Method = type.getDeclaredMethod(name, *parameters)

    private val twoArgConstructor get() = constructor(eventType, string)
    private val fourArgConstructor get() = constructor(eventType, string, string, string)
    private val fourArgDefaultConstructor get() = constructor(eventType, string, string, string, int, marker)
    private val twoArgCopy get() = method("copy", eventType, string)
    private val twoArgCopyDefault get() = method("copy\$default", type, eventType, string, int, Any::class.java)
    private val fourArgCopy get() = method("copy", eventType, string, string, string)
    private val fourArgCopyDefault
        get() = method("copy\$default", type, eventType, string, string, string, int, Any::class.java)

    private val challengeResult = Standalone3DSResult(
        event = StandaloneEventType.CHARGE_AUTH_SUCCESS,
        charge3dsId = CHARGE_ID,
        status = "success",
        resultDescription = "challenge",
        authenticationFlow = Standalone3DSAuthenticationFlow.CHALLENGE,
    )

    @Test
    fun `every bridge JVM signature exists`() {
        twoArgConstructor
        fourArgConstructor
        fourArgDefaultConstructor
        twoArgCopy
        twoArgCopyDefault
        fourArgCopy
        fourArgCopyDefault
        assertTrue(Modifier.isStatic(twoArgCopyDefault.modifiers))
        assertTrue(Modifier.isStatic(fourArgCopyDefault.modifiers))
    }

    @Test
    fun `the bridges are synthetic, hidden from Java`() {
        listOf(twoArgConstructor, fourArgConstructor, twoArgCopy, fourArgCopy).forEach { bridge ->
            assertTrue(bridge.isSynthetic, "$bridge is synthetic")
        }
    }

    @Test
    fun `the 2-argument constructor gives a null flow`() {
        val result = twoArgConstructor.newInstance(StandaloneEventType.CHARGE_AUTH_SUCCESS, CHARGE_ID)

        assertEquals(Standalone3DSResult(StandaloneEventType.CHARGE_AUTH_SUCCESS, CHARGE_ID), result)
        assertNull(result.authenticationFlow)
    }

    @Test
    fun `the 4-argument constructor gives a null flow`() {
        val result = fourArgConstructor.newInstance(StandaloneEventType.CHARGE_AUTH_SUCCESS, CHARGE_ID, "success", "x")

        assertEquals(
            Standalone3DSResult(StandaloneEventType.CHARGE_AUTH_SUCCESS, CHARGE_ID, "success", "x", null),
            result
        )
        assertNull(result.authenticationFlow)
    }

    @Test
    fun `the 4-argument default constructor gives a null flow`() {
        // Mask 0b1100: status and resultDescription omitted, as a 5.7.x call site with two arguments compiles.
        val result = fourArgDefaultConstructor.newInstance(
            StandaloneEventType.CHARGE_AUTH_REJECT,
            CHARGE_ID,
            "ignored",
            "ignored",
            MASK_LAST_TWO_OF_FOUR,
            null
        )

        assertEquals(Standalone3DSResult(StandaloneEventType.CHARGE_AUTH_REJECT, CHARGE_ID), result)
        assertNull(result.authenticationFlow)
    }

    @Test
    fun `the 2-argument copy keeps the flow`() {
        val copied = twoArgCopy.invoke(challengeResult, StandaloneEventType.CHARGE_AUTH_REJECT, "other")
            as Standalone3DSResult

        assertEquals(
            challengeResult.copy(event = StandaloneEventType.CHARGE_AUTH_REJECT, charge3dsId = "other"),
            copied
        )
        assertEquals(Standalone3DSAuthenticationFlow.CHALLENGE, copied.authenticationFlow)
    }

    @Test
    fun `the 2-argument copy default keeps the flow`() {
        // Mask 0b10: charge3dsId omitted.
        val copied = twoArgCopyDefault.invoke(
            null,
            challengeResult,
            StandaloneEventType.CHARGE_AUTH_REJECT,
            null,
            MASK_SECOND,
            null
        ) as Standalone3DSResult

        assertEquals(challengeResult.copy(event = StandaloneEventType.CHARGE_AUTH_REJECT), copied)
        assertEquals(Standalone3DSAuthenticationFlow.CHALLENGE, copied.authenticationFlow)
    }

    @Test
    fun `the 4-argument copy keeps the flow`() {
        val copied = fourArgCopy.invoke(challengeResult, StandaloneEventType.CHARGE_AUTH_REJECT, CHARGE_ID, "s", "d")
            as Standalone3DSResult

        assertEquals(
            challengeResult.copy(
                event = StandaloneEventType.CHARGE_AUTH_REJECT,
                status = "s",
                resultDescription = "d"
            ),
            copied
        )
        assertEquals(Standalone3DSAuthenticationFlow.CHALLENGE, copied.authenticationFlow)
    }

    @Test
    fun `the 4-argument copy default keeps the flow`() {
        // Mask 0b1110: only event given.
        val copied = fourArgCopyDefault.invoke(
            null,
            challengeResult,
            StandaloneEventType.CHARGE_AUTH_REJECT,
            null,
            null,
            null,
            MASK_ALL_BUT_FIRST_OF_FOUR,
            null
        ) as Standalone3DSResult

        assertEquals(challengeResult.copy(event = StandaloneEventType.CHARGE_AUTH_REJECT), copied)
        assertEquals(Standalone3DSAuthenticationFlow.CHALLENGE, copied.authenticationFlow)
    }

    private companion object {
        const val CHARGE_ID = "charge-3ds-id"
        const val MASK_SECOND = 0b10
        const val MASK_LAST_TWO_OF_FOUR = 0b1100
        const val MASK_ALL_BUT_FIRST_OF_FOUR = 0b1110
    }
}
