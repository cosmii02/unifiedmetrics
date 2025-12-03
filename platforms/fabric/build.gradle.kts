import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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

plugins {
    id("fabric-loom")
    id("com.github.johnrengelman.shadow")
}

val transitiveInclude: Configuration by configurations.creating {
    exclude(group = "com.mojang")
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "org.jetbrains.kotlinx")
}

dependencies {
    // https://fabricmc.net/versions.html
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.13")

    modImplementation("net.fabricmc.fabric-api:fabric-api:0.92.2+1.20.1")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.2+kotlin.2.1.20")

    api(project(":unifiedmetrics-core"))

    transitiveInclude(project(":unifiedmetrics-api"))
    transitiveInclude(project(":unifiedmetrics-common"))
    transitiveInclude(project(":unifiedmetrics-core"))
    transitiveInclude(project(":unifiedmetrics-driver-influx"))
    transitiveInclude(project(":unifiedmetrics-driver-prometheus"))
    transitiveInclude("com.charleskorn.kaml:kaml:0.76.0")
    transitiveInclude("it.krzeminski:snakeyaml-engine-kmp:3.1.1")
    transitiveInclude("net.thauvin.erik.urlencoder:urlencoder-lib-jvm:1.6.0")
    transitiveInclude("com.squareup.okio:okio-jvm:3.11.0")
    transitiveInclude("org.jetbrains.kotlin:kotlin-stdlib:2.1.20")
    transitiveInclude("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    transitiveInclude("io.prometheus:simpleclient:0.16.0")
    transitiveInclude("io.prometheus:simpleclient_common:0.16.0")
    transitiveInclude("io.prometheus:simpleclient_tracer_common:0.16.0")
    transitiveInclude("io.prometheus:simpleclient_tracer_otel:0.16.0")
    transitiveInclude("io.prometheus:simpleclient_tracer_otel_agent:0.16.0")
    transitiveInclude("io.prometheus:simpleclient_httpserver:0.16.0")
    transitiveInclude("io.prometheus:simpleclient_pushgateway:0.16.0")
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(transitiveInclude)
    archiveClassifier.set("dev-shadow")
}

tasks.named<RemapJarTask>("remapJar") {
    dependsOn("shadowJar")
    inputFile.set(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
    archiveClassifier.set(null as String?)
}

loom {
    runs {
        named("server") {
            isIdeConfigGenerated = true
        }
    }
    serverOnlyMinecraftJar()
}

tasks {
    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }
    processResources {
        filesMatching("fabric.mod.json") {
            expand(
                "version" to project.version
            )
        }
    }
    compileJava {
        options.encoding = "UTF-8"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
