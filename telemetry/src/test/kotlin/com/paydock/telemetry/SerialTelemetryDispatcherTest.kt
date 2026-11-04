package com.paydock.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Unit tests for the one telemetry worker (R_02): a daemon thread, serial, bounded, and never killed by a task. */
internal class SerialTelemetryDispatcherTest {

    private val waitSeconds = 5L

    /** Blocks the worker until [release] is counted down; returns once the worker is inside the blocking task. */
    private fun SerialTelemetryDispatcher.block(release: CountDownLatch) {
        val running = CountDownLatch(1)
        val accepted = post {
            running.countDown()
            release.await()
        }
        assertTrue(accepted)
        assertTrue(running.await(waitSeconds, TimeUnit.SECONDS))
    }

    @Test
    fun runsOnOneDaemonThreadNamedPaydockTelemetry() {
        val dispatcher = SerialTelemetryDispatcher()
        val threads = Collections.synchronizedList(mutableListOf<Thread>())
        val done = CountDownLatch(3)

        repeat(3) {
            val accepted = dispatcher.post {
                threads += Thread.currentThread()
                done.countDown()
            }
            assertTrue(accepted)
        }

        assertTrue(done.await(waitSeconds, TimeUnit.SECONDS))
        assertEquals(1, threads.toSet().size)
        assertEquals(SerialTelemetryDispatcher.THREAD_NAME, threads.first().name)
        assertEquals("paydock-telemetry", threads.first().name)
        assertTrue(threads.first().isDaemon)
    }

    @Test
    fun keepsTheOrderOfPostedTasks() {
        val dispatcher = SerialTelemetryDispatcher()
        val order = Collections.synchronizedList(mutableListOf<Int>())
        val done = CountDownLatch(1)

        (0 until 100).forEach { index -> assertTrue(dispatcher.post { order += index }) }
        dispatcher.post { done.countDown() }

        assertTrue(done.await(waitSeconds, TimeUnit.SECONDS))
        assertEquals((0 until 100).toList(), order.toList())
    }

    @Test
    fun aFullQueue_returnsFalseAtOnce() {
        val dispatcher = SerialTelemetryDispatcher(capacity = 2)
        val release = CountDownLatch(1)
        val ran = Collections.synchronizedList(mutableListOf<Int>())
        try {
            dispatcher.block(release)
            assertTrue(dispatcher.post { ran += 1 })
            assertTrue(dispatcher.post { ran += 2 })

            val startedAt = System.nanoTime()
            val accepted = dispatcher.post { ran += 3 }
            val elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)

            assertFalse(accepted)
            assertTrue("post took $elapsedMillis ms", elapsedMillis < 100)
        } finally {
            release.countDown()
        }
        val done = CountDownLatch(1)
        assertTrue(dispatcher.post { done.countDown() })
        assertTrue(done.await(waitSeconds, TimeUnit.SECONDS))
        assertEquals(listOf(1, 2), ran.toList())
    }

    @Test
    fun aThrowingTask_doesNotStopTheNext() {
        val dispatcher = SerialTelemetryDispatcher()
        val done = CountDownLatch(1)

        assertTrue(dispatcher.post { error("synthetic task failure") })
        assertTrue(dispatcher.post { throw OutOfMemoryError("synthetic error") })
        assertTrue(dispatcher.post { done.countDown() })

        assertTrue(done.await(waitSeconds, TimeUnit.SECONDS))
    }

    @Test
    fun postDelayed_runsLater() {
        val dispatcher = SerialTelemetryDispatcher()
        val postedAt = System.nanoTime()
        var ranAfterNanos = -1L
        val done = CountDownLatch(1)

        assertTrue(
            dispatcher.postDelayed(200) {
                ranAfterNanos = System.nanoTime() - postedAt
                done.countDown()
            }
        )

        assertTrue(done.await(waitSeconds, TimeUnit.SECONDS))
        assertTrue("ran after $ranAfterNanos ns", TimeUnit.NANOSECONDS.toMillis(ranAfterNanos) >= 200)
    }

    @Test
    fun noThreadIsCreatedAtConstruction() {
        val before = Thread.getAllStackTraces().keys.count { it.name == SerialTelemetryDispatcher.THREAD_NAME }

        SerialTelemetryDispatcher()

        val after = Thread.getAllStackTraces().keys.count { it.name == SerialTelemetryDispatcher.THREAD_NAME }
        assertEquals(before, after)
    }
}
