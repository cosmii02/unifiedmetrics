/*
 *     This file is part of UnifiedMetrics.
 *
 *     UnifiedMetrics is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     UnifiedMetrics is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with UnifiedMetrics.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.cubxity.plugins.metrics.prometheus.exporter

import com.sun.net.httpserver.BasicAuthenticator
import dev.cubxity.plugins.metrics.api.UnifiedMetrics
import dev.cubxity.plugins.metrics.prometheus.PrometheusMetricsDriver
import dev.cubxity.plugins.metrics.prometheus.collector.UnifiedMetricsCollector
import dev.cubxity.plugins.metrics.prometheus.config.AuthenticationScheme
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.HTTPServer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class PrometheusHTTPExporter(
    private val api: UnifiedMetrics,
    private val driver: PrometheusMetricsDriver
) : PrometheusExporter {
    private var server: HTTPServer? = null
    private var executor: ExecutorService? = null
    private val pluginClassLoader = UnifiedMetricsCollector::class.java.classLoader
    private val threadCounter = AtomicInteger()

    override fun initialize() {
        val registry = CollectorRegistry()
        registry.register(UnifiedMetricsCollector(api))

        val service = Executors.newCachedThreadPool { task ->
            Thread(task, "prometheus-http-${driver.config.http.port}-${threadCounter.incrementAndGet()}").apply {
                isDaemon = true
                contextClassLoader = pluginClassLoader
            }
        }
        executor = service

        val httpServer = HTTPServer.Builder()
            .withHostname(driver.config.http.host)
            .withPort(driver.config.http.port)
            .withRegistry(registry)
            .withExecutorService(service)
            .apply {
                with(driver.config.http.authentication) {
                    if (scheme == AuthenticationScheme.Basic) {
                        withAuthenticator(Authenticator(username, password))
                    }
                }
            }
            .build()

        server = httpServer
    }

    override fun close() {
        server?.close()
        server = null
        executor?.shutdownNow()
        executor = null
    }

    private class Authenticator(
        private val username: String,
        private val password: String
    ) : BasicAuthenticator("unifiedmetrics") {
        override fun checkCredentials(username: String?, password: String?): Boolean =
            this.username == username && this.password == password
    }
}
