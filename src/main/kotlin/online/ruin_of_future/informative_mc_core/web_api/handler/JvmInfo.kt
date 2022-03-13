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
import online.ruin_of_future.informative_mc_core.data.ModData
import online.ruin_of_future.informative_mc_core.token_system.TokenManager
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream

val JvmInfoApiID = ApiID("system-info", "jvm-info")

// TODO: Lift `requestStatus` and `requestInfo` out.

@Suppress("UnUsed")
@Serializable
class JvmInfoResponse(
    val requestStatus: String,
    val requestInfo: String,
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
        fun getForNow(): JvmInfoResponse {
            return JvmInfoResponse(
                requestStatus = "success",
                requestInfo = "",
                jvmName = System.getProperty("java.vm.name") ?: "unknown",
                jvmVendor = System.getProperty("java.vm.vendor") ?: "unknown",
                jvmVersion = System.getProperty("java.vm.version") ?: "unknown",
                jvmInfo = System.getProperty("Java.vm.info") ?: "unknown",
                javaVersion = System.getProperty("java.version") ?: "unknown",
                kotlinVersion = KotlinVersion.CURRENT.toString(),
            )
        }

        fun unknownUser(userName: String): JvmInfoResponse {
            return JvmInfoResponse(
                requestStatus = "error",
                requestInfo = "unknown user: $userName",
                jvmName = "",
                jvmVendor = "",
                jvmVersion = "",
                jvmInfo = "",
                javaVersion = "",
                kotlinVersion = "",
            )
        }

        fun invalidToken(): JvmInfoResponse {
            return JvmInfoResponse(
                requestStatus = "error",
                requestInfo = "invalid token",
                jvmName = "",
                jvmVendor = "",
                jvmVersion = "",
                jvmInfo = "",
                javaVersion = "",
                kotlinVersion = "",
            )
        }
    }
}

class JvmInfoHandler(
    private val tokenManager: TokenManager,
    private val modData: ModData,
) : ParamPostHandler() {
    override val id: ApiID = JvmInfoApiID

    override fun handleRequest(
        formParams: Map<String, List<String>>,
        outputStream: OutputStream
    ) {
        val req = parseUserRequest(formParams)
        if (!modData.hasUserName(req.userName)) {
            JvmInfoResponse.unknownUser(req.userName)
        } else if (!tokenManager.verify(req.token)) {
            JvmInfoResponse.invalidToken().writeToStream(outputStream)
        } else {
            JvmInfoResponse.getForNow().writeToStream(outputStream)
        }
    }
}
