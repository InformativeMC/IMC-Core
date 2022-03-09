/*
 * Copyright (c) 2022 InformativeMC
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/lgpl-3.0.txt>.
 */
package online.ruin_of_future.informative_mc_core.web_api.handler

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream

val JvmInfoApiID = ApiID("system-info", "jvm-info")

@Suppress("UnUsed")
@OptIn(ExperimentalSerializationApi::class)
@Serializable
class JvmInfo private constructor(
    // Jvm Info
    val jvmName: String,
    val jvmVendor: String,
    val jvmVersion: String,
    val jvmInfo: String,

    // Java & Kotlin version
    val javaVersion: String,
    val kotlinVersion: String,
    override val id: ApiID = JvmInfoApiID,
) : ParaFreeApiHandler() {
    constructor() : this("???", "???", "???", "???", "???", "???")

    override fun handleRequest(outputStream: OutputStream) {
        val info = JvmInfo(
            System.getProperty("java.vm.name") ?: "unknown",
            System.getProperty("java.vm.vendor") ?: "unknown",
            System.getProperty("java.vm.version") ?: "unknown",
            System.getProperty("Java.vm.info") ?: "unknown",
            System.getProperty("java.version") ?: "unknown",
            KotlinVersion.CURRENT.toString(),
        )
        Json.encodeToStream(info, outputStream)
    }
}
