package com.paydock.sample.debug.telemetry

import com.paydock.core.domain.model.Environment
import com.paydock.sample.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketAddress
import java.net.URI
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread

/**
 * A local stand-in proxy for the SDK's API host only. REFUSE points the host at a closed local port (connection
 * failure); HANG accepts the CONNECT and never answers (timeout after the client's 60 s read timeout).
 *
 * OkHttp reads the default ProxySelector once, when the SDK builds its client. So [install] sets a pass-through
 * selector as the JVM default at app start (debug builds only, before the SDK initialises). It hands every request to
 * the previous selector unless [around] is running, which routes the API host to the stand-in for that one call.
 * Other hosts always go to the previous selector.
 */
class StandInProxy {

    enum class Mode { REFUSE, HANG }

    /** Sets the pass-through selector as the JVM default, once per process. Call before the SDK initialises. */
    fun install() {
        selector
    }

    /** Runs [block] with the API host routed to the stand-in; one call at a time, the route cleared after. */
    suspend fun <T> around(mode: Mode, block: suspend () -> T): T = callLock.withLock {
        val passThrough = selector
        val server = if (mode == Mode.HANG) withContext(Dispatchers.IO) { HangingServer() } else null
        val port = server?.port ?: withContext(Dispatchers.IO) { closedPort() }
        passThrough.standIn = Proxy(Proxy.Type.HTTP, InetSocketAddress(InetAddress.getLoopbackAddress(), port))
        try {
            block()
        } finally {
            passThrough.standIn = null
            server?.close()
        }
    }

    /** Hands every request to [previous], except the API host while a stand-in is set. */
    private class PassThroughSelector(
        private val previous: ProxySelector?,
        private val host: String,
    ) : ProxySelector() {
        @Volatile
        var standIn: Proxy? = null

        override fun select(uri: URI?): List<Proxy> {
            val proxy = standIn
            return if (proxy != null && uri.isApiHost()) {
                listOf(proxy)
            } else {
                previous?.select(uri) ?: listOf(Proxy.NO_PROXY)
            }
        }

        override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {
            if (standIn == null || !uri.isApiHost()) previous?.connectFailed(uri, sa, ioe)
        }

        private fun URI?.isApiHost(): Boolean = this?.host.equals(host, ignoreCase = true)
    }

    /** Accepts every connection and never answers, until closed. */
    private class HangingServer : Closeable {
        private val server = ServerSocket(0, BACKLOG, InetAddress.getLoopbackAddress())
        private val accepted = CopyOnWriteArrayList<Socket>()
        val port: Int = server.localPort

        init {
            thread(isDaemon = true, name = "stand-in-proxy") {
                runCatching {
                    while (!server.isClosed) accepted += server.accept()
                }
            }
        }

        override fun close() {
            runCatching { server.close() }
            accepted.forEach { socket -> runCatching { socket.close() } }
        }
    }

    private companion object {
        const val BACKLOG = 8

        /** The process-wide pass-through selector, set as the JVM default on first use. */
        val selector: PassThroughSelector by lazy {
            PassThroughSelector(
                previous = ProxySelector.getDefault(),
                host = apiHostOf(BuildConfig.SDK_ENVIRONMENT)
            ).also(ProxySelector::setDefault)
        }

        val callLock = Mutex()

        /** A local port nothing listens on: bound, read, then released. */
        fun closedPort(): Int = ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { it.localPort }

        /**
         * The SDK's API host per environment. A copy of the SDK's internal mapping in
         * `com.paydock.core.domain.mapper.EnvironmentMapper` (`mapToBaseUrl`); keep the two in step.
         */
        fun apiHostOf(environment: Environment): String = when (environment) {
            Environment.PRODUCTION -> "api.paydock.com"
            Environment.SANDBOX -> "api-sandbox.paydock.com"
            Environment.STAGING -> "apista.paydock.com"
        }
    }
}
