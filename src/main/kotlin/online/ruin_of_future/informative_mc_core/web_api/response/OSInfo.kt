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
import online.ruin_of_future.informative_mc_core.util.humanReadableSize

@Suppress("UnUsed")
@Serializable
class OSInfoResponseDetail(
    // OS Info
    val osName: String,
    val maxMemory: String,
    val allocatedMemory: String,
    val freeMemory: String,
) {
    companion object {
        fun getCurrent(): OSInfoResponseDetail {
            return OSInfoResponseDetail(
                osName = System.getProperty("os.name") ?: "unknown",
                maxMemory = Runtime.getRuntime().maxMemory().humanReadableSize(),
                allocatedMemory = Runtime.getRuntime().totalMemory().humanReadableSize(),
                freeMemory = Runtime.getRuntime().freeMemory().humanReadableSize(),
            )
        }
    }
}

@Suppress("UnUsed")
@Serializable
class OSInfoResponse(
    override val requestStatus: String,
    override val requestInfo: String,
    override val responseDetail: OSInfoResponseDetail?
) : ApiResponse<OSInfoResponseDetail>() {

    companion object CommonResponses : ApiAuthCommonResponse<OSInfoResponseDetail, OSInfoResponse>(
        responseBuilder = { status, info, detail -> OSInfoResponse(status, info, detail) }
    )
}