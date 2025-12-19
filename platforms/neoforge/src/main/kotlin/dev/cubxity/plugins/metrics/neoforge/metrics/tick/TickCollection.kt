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

package dev.cubxity.plugins.metrics.neoforge.metrics.tick

import dev.cubxity.plugins.metrics.api.metric.collector.Collector
import dev.cubxity.plugins.metrics.api.metric.collector.CollectorCollection
import dev.cubxity.plugins.metrics.api.metric.collector.Histogram
import dev.cubxity.plugins.metrics.api.metric.collector.MILLISECONDS_PER_SECOND
import dev.cubxity.plugins.metrics.api.metric.store.VolatileDoubleStore
import dev.cubxity.plugins.metrics.api.metric.store.VolatileLongStore
import dev.cubxity.plugins.metrics.common.metric.Metrics
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.tick.ServerTickEvent

class TickCollection : CollectorCollection {
    private val tickDuration = Histogram(
        Metrics.Server.TickDurationSeconds,
        sumStoreFactory = VolatileDoubleStore,
        countStoreFactory = VolatileLongStore
    )

    private var lastTickStart = 0L

    override val collectors: List<Collector> = listOf(tickDuration)

    override fun initialize() {
        NeoForge.EVENT_BUS.register(this)
    }

    override fun dispose() {
        NeoForge.EVENT_BUS.unregister(this)
    }

    @SubscribeEvent
    fun onServerTickStart(event: ServerTickEvent.Pre) {
        lastTickStart = System.nanoTime()
    }

    @SubscribeEvent
    fun onServerTickEnd(event: ServerTickEvent.Post) {
        if (lastTickStart != 0L) {
            val durationMillis = (System.nanoTime() - lastTickStart) / 1_000_000.0
            tickDuration += durationMillis / MILLISECONDS_PER_SECOND
        }
    }
}
