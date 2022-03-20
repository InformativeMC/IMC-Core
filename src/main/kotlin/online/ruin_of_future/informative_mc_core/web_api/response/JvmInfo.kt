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
package online.ruin_of_future.informative_mc_core.web_api.response

import kotlinx.serialization.Serializable

@Suppress("UnUsed")
@Serializable
class JvmInfoResponseBody(
    // Jvm Info
    val jvmName: String,
    val jvmVendor: String,
    val jvmVersion: String,
    val jvmInfo: String,
    // Java & Kotlin version
    val javaVersion: String,
    val kotlinVersion: String,
) {
    companion object {
        fun getCurrent(): JvmInfoResponseBody {
            return JvmInfoResponseBody(
                jvmName = System.getProperty("java.vm.name") ?: "unknown",
                jvmVendor = System.getProperty("java.vm.vendor") ?: "unknown",
                jvmVersion = System.getProperty("java.vm.version") ?: "unknown",
                jvmInfo = System.getProperty("Java.vm.info") ?: "unknown",
                javaVersion = System.getProperty("java.version") ?: "unknown",
                kotlinVersion = KotlinVersion.CURRENT.toString(),
            )
        }
    }
}


@Suppress("UnUsed")
@Serializable
class JvmInfoResponse(
    override val requestStatus: String,
    override val requestInfo: String,
    override val responseBody: JvmInfoResponseBody?
) : ApiResponse<JvmInfoResponseBody>() {
    companion object CommonResponses : ApiAuthCommonResponses<JvmInfoResponseBody>(
        responseBuilder = { status, info, body -> JvmInfoResponse(status, info, body) }
    )
}