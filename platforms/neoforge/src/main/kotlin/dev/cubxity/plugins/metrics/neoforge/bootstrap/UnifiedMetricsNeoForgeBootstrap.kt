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

package dev.cubxity.plugins.metrics.neoforge.bootstrap

import dev.cubxity.plugins.metrics.api.platform.PlatformType
import dev.cubxity.plugins.metrics.common.UnifiedMetricsBootstrap
import dev.cubxity.plugins.metrics.common.plugin.dispatcher.CurrentThreadDispatcher
import dev.cubxity.plugins.metrics.neoforge.UnifiedMetricsNeoForgePlugin
import dev.cubxity.plugins.metrics.neoforge.logger.Log4jLogger
import kotlinx.coroutines.CoroutineDispatcher
import net.minecraft.server.MinecraftServer
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import org.apache.logging.log4j.LogManager
import java.nio.file.Path

@Mod("unifiedmetrics")
class UnifiedMetricsNeoForgeBootstrap : UnifiedMetricsBootstrap {
    private val plugin = UnifiedMetricsNeoForgePlugin(this)
    lateinit var server: MinecraftServer
        private set

    override val type: PlatformType
        get() = PlatformType.NeoForge

    override val version: String = ModList.get().getModContainerById("unifiedmetrics")
        .map { it.modInfo.version.toString() }
        .orElse("<unknown>")

    override val serverBrand: String
        get() = server.serverModName

    override val dataDirectory: Path = FMLPaths.CONFIGDIR.get().resolve("unifiedmetrics")

    override val configDirectory: Path = FMLPaths.CONFIGDIR.get().resolve("unifiedmetrics")

    override val logger = Log4jLogger(LogManager.getLogger("UnifiedMetrics"))

    override val dispatcher: CoroutineDispatcher = CurrentThreadDispatcher

    private val previousExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logger.severe("UnifiedMetrics caught an unhandled exception on thread ${thread.name}", throwable)
            previousExceptionHandler?.uncaughtException(thread, throwable)
        }

        NeoForge.EVENT_BUS.addListener(this::onServerStarted)
        NeoForge.EVENT_BUS.addListener(this::onServerStopping)
    }

    private fun onServerStarted(event: ServerStartedEvent) {
        server = event.server
        runCatching {
            plugin.enable()
            logger.info("UnifiedMetrics NeoForge plugin enabled for ${serverBrand}.")
        }.onFailure { error ->
            logger.severe("UnifiedMetrics failed to enable on NeoForge.", error)
        }
    }

    private fun onServerStopping(event: ServerStoppingEvent) {
        runCatching {
            plugin.disable()
            logger.info("UnifiedMetrics NeoForge plugin disabled cleanly.")
        }.onFailure { error ->
            logger.severe("UnifiedMetrics encountered an error while shutting down.", error)
        }
    }
}
