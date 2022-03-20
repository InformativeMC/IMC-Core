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

import online.ruin_of_future.informative_mc_core.web_api.id.HeartbeatApiId
import online.ruin_of_future.informative_mc_core.web_api.id.JvmInfoApiId
import online.ruin_of_future.informative_mc_core.web_api.id.OSInfoApiId
import online.ruin_of_future.informative_mc_core.web_api.response.HeartbeatResponse
import online.ruin_of_future.informative_mc_core.web_api.response.JvmInfoResponse
import online.ruin_of_future.informative_mc_core.web_api.response.OSInfoResponse
import java.util.*

private class HeartbeatTest
    : ApiTest<HeartbeatResponse> by GetApiTestImpl(
    HeartbeatApiId,
    HeartbeatResponse.serializer(),
) {
    override fun checkResponse(response: HeartbeatResponse) {
        super.checkResponse(response)
        assert(response.responseDetail?.status == "healthy")
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