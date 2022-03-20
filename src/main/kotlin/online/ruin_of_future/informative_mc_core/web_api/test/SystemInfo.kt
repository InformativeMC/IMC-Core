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
package online.ruin_of_future.informative_mc_core.web_api.test

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Request
import online.ruin_of_future.informative_mc_core.web_api.id.HeartbeatApiId
import online.ruin_of_future.informative_mc_core.web_api.id.JvmInfoApiId
import online.ruin_of_future.informative_mc_core.web_api.id.OSInfoApiId
import online.ruin_of_future.informative_mc_core.web_api.response.HeartbeatResponse
import online.ruin_of_future.informative_mc_core.web_api.response.JvmInfoResponse
import online.ruin_of_future.informative_mc_core.web_api.response.OSInfoResponse
import java.util.*

private class HeartbeatTest : ApiTest<HeartbeatResponse> {
    override val apiId = HeartbeatApiId

    override suspend fun runWithCallback(
        onSuccess: (response: HeartbeatResponse) -> Unit,
        onFailure: (cause: Throwable) -> Unit,
    ) {
        val request = Request
            .Builder()
            .url(apiAddress)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.code != 200) {
                assert(false)
            } else {
                val body = Json.decodeFromString<HeartbeatResponse>(response.body!!.string())
                assert(body.requestStatus == "success")
                assert(body.responseDetail?.status == "healthy")
                onSuccess(body)
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}

private class JvmInfoTest(
    username: String,
    tokenUUID: UUID,
) : ApiTest<JvmInfoResponse> by PostApiTestImpl(
    JvmInfoApiId,
    username,
    tokenUUID,
    JvmInfoResponse.serializer()
)

private class OSInfoTest(
    private val username: String,
    private val tokenUUID: UUID,
) : ApiTest<OSInfoResponse> by PostApiTestImpl(
    OSInfoApiId,
    username,
    tokenUUID,
    OSInfoResponse.serializer()
)

class SystemInfoTestBatch(
    private val username: String,
    private val tokenUUID: UUID,
) : ApiTestBatch by ApiTestBatchAsync(
    "System Info",
    listOf<ApiTest<*>>(
        HeartbeatTest(),
        JvmInfoTest(username, tokenUUID),
        OSInfoTest(username, tokenUUID)
    )
)