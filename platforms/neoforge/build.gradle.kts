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

import org.gradle.api.artifacts.Configuration

plugins {
    id("net.neoforged.moddev")
}

val jarJar: Configuration by configurations.getting

repositories {
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    implementation(project(":unifiedmetrics-core"))

    jarJar(project(":unifiedmetrics-api"))
    jarJar(project(":unifiedmetrics-common")) {
        isTransitive = true
    }
    jarJar(project(":unifiedmetrics-core"))
    jarJar(project(":unifiedmetrics-driver-influx")) {
        isTransitive = true
    }
    jarJar(project(":unifiedmetrics-driver-prometheus"))

    jarJar("com.charleskorn.kaml:kaml-jvm:0.76.0")
    jarJar("it.krzeminski:snakeyaml-engine-kmp-jvm:3.1.1")
    jarJar("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.7.3")
    jarJar("net.thauvin.erik.urlencoder:urlencoder-lib-jvm:1.6.0")
    jarJar("com.squareup.okio:okio-jvm:3.11.0")
    jarJar("org.jetbrains.kotlin:kotlin-stdlib:2.1.20")
    jarJar("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.2")
    jarJar("io.prometheus:simpleclient:0.16.0")
    jarJar("io.prometheus:simpleclient_common:0.16.0")
    jarJar("io.prometheus:simpleclient_tracer_common:0.16.0")
    jarJar("com.influxdb:influxdb-client-java:7.2.0")
    jarJar(project(":unifiedmetrics-driver-prometheus-exporters"))
}

neoForge {
    version.set("21.1.215")

    mods {
        create("unifiedmetrics") {
            sourceSet(sourceSets.main.get())
        }
    }
}

tasks {
    processResources {
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(
                "version" to project.version
            )
        }
    }
}
