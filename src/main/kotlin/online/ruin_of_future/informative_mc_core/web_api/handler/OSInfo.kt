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

import kotlinx.serialization.Serializable
import online.ruin_of_future.informative_mc_core.humanReadableSize
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream

val OSInfoApiId = ApiID("system-info", "os-info")

// TODO: Change it to POST handler with authentication
// TODO: Separate handler and response.

@Suppress("UnUsed")
@Serializable
class OSInfo private constructor(
    // OS Info
    val osName: String,
    val maxMemory: String,
    val allocatedMemory: String,
    val freeMemory: String,
    override val id: ApiID = OSInfoApiId,
) : ParamFreeHandler() {

    constructor() : this("???", "???", "???", "???")

    override fun handleRequest(outputStream: OutputStream) {
        OSInfo(
            System.getProperty("os.name") ?: "unknown",
            Runtime.getRuntime().maxMemory().humanReadableSize(),
            Runtime.getRuntime().totalMemory().humanReadableSize(),
            Runtime.getRuntime().freeMemory().humanReadableSize(),
        ).writeToStream(outputStream)
    }
}