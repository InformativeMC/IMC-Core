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
import online.ruin_of_future.informative_mc_core.util.humanReadableSize
import online.ruin_of_future.informative_mc_core.token_system.TokenManager
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream

val OSInfoApiId = ApiID("system-info", "os-info")

// TODO: Lift `requestStatus` and `requestInfo` out.

@Suppress("UnUsed")
@Serializable
class OSInfoResponse(
    val requestStatus: String,
    val requestInfo: String,
    // OS Info
    val osName: String,
    val maxMemory: String,
    val allocatedMemory: String,
    val freeMemory: String,
) {
    companion object {
        fun getForNow(): OSInfoResponse {
            return OSInfoResponse(
                requestStatus = "success",
                requestInfo = "",
                osName = System.getProperty("os.name") ?: "unknown",
                maxMemory = Runtime.getRuntime().maxMemory().humanReadableSize(),
                allocatedMemory = Runtime.getRuntime().totalMemory().humanReadableSize(),
                freeMemory = Runtime.getRuntime().freeMemory().humanReadableSize(),
            )
        }

        fun unknownUser(userName: String): OSInfoResponse {
            return OSInfoResponse(
                requestStatus = "error",
                requestInfo = "unknown user: $userName",
                osName = "", maxMemory = "", allocatedMemory = "", freeMemory = "",
            )
        }

        fun invalidToken(): OSInfoResponse {
            return OSInfoResponse(
                requestStatus = "error",
                requestInfo = "invalid token",
                osName = "", maxMemory = "", allocatedMemory = "", freeMemory = "",
            )
        }
    }
}

class OSInfoHandler(
    private val tokenManager: TokenManager,
    private val modData: ModData,
) : ParamPostHandler() {
    override val id: ApiID = OSInfoApiId
    override fun handleRequest(
        formParams: Map<String, List<String>>,
        outputStream: OutputStream
    ) {
        val req = parseUserRequest(formParams)
        if (!modData.hasUserName(req.userName)) {
            OSInfoResponse.unknownUser(req.userName).writeToStream(outputStream)
        } else if (!tokenManager.verify(req.token)) {
            OSInfoResponse.invalidToken().writeToStream(outputStream)
        } else {
            OSInfoResponse.getForNow().writeToStream(outputStream)
        }
    }
}