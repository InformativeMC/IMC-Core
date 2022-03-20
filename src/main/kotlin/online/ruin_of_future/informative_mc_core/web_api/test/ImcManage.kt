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

import online.ruin_of_future.informative_mc_core.util.generateRandomString
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import online.ruin_of_future.informative_mc_core.web_api.id.UserRegisterApiId
import online.ruin_of_future.informative_mc_core.web_api.id.UserTestApiId
import online.ruin_of_future.informative_mc_core.web_api.response.ApiResponse
import online.ruin_of_future.informative_mc_core.web_api.response.UserRegisterResponse
import online.ruin_of_future.informative_mc_core.web_api.response.UserTestResponse
import java.util.*

private class UserRegisterTest(
    private val tmpAuthUUID: UUID,
    private val username: String,
) : ApiTest<UserRegisterResponse> by PostApiTestImpl(
    UserRegisterApiId,
    username,
    tmpAuthUUID,
    UserRegisterResponse.serializer()
) {
    override fun checkResponse(response: UserRegisterResponse) {
        super.checkResponse(response)
        assert(response.responseDetail?.userName == username) { "not a valid username" }
    }
}

private class UserTestTest(
    private val username: String,
    private val tokenUUID: UUID,
) : ApiTest<UserTestResponse> by PostApiTestImpl(
    UserTestApiId,
    username,
    tokenUUID,
    UserTestResponse.serializer()
) {
    override fun checkResponse(response: UserTestResponse) {
        super.checkResponse(response)
        assert(response.responseDetail?.username == username) { "not a valid username" }
    }
}

class ImcManageTestBatch(
    private val tmpAuthUUID: UUID
) : ApiTestBatch {
    override val name = "IMC Manage"
    override val passedTest = mutableMapOf<ApiId, ApiResponse<*>>()
    override val failedTest = mutableMapOf<ApiId, Throwable>()

    private val username = "TEST_${generateRandomString(5)}"
    private lateinit var tokenUUID: UUID
    override suspend fun runWithCallback(
        onSuccess: (passedTest: Map<ApiId, ApiResponse<*>>) -> Unit,
        onFailure: (failedTest: Map<ApiId, Throwable>) -> Unit
    ) {
        UserRegisterTest(tmpAuthUUID, username).runWithCallback(
            onSuccess = {
                passedTest[UserRegisterApiId] = it
                tokenUUID = it.responseDetail?.uuid!!
            },
            onFailure = {
                failedTest[UserRegisterApiId] = it
            },
        )
        UserTestTest(username, tokenUUID).runWithCallback(
            onSuccess = {
                passedTest[UserTestApiId] = it
            },
            onFailure = {
                failedTest[UserTestApiId] = it
            },
        )
        onSuccess(passedTest)
        onFailure(failedTest)
    }
}