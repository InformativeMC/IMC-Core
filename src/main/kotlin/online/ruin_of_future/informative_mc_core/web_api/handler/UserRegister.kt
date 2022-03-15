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
import online.ruin_of_future.informative_mc_core.auth.TokenManager
import online.ruin_of_future.informative_mc_core.data.ImcUser
import online.ruin_of_future.informative_mc_core.data.ModData
import online.ruin_of_future.informative_mc_core.util.UUIDSerializer
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream
import java.util.*

val UserRegisterApiId = ApiID("imc-manage", "register")

// TODO: Lift `requestStatus` and `requestInfo` out.

@Serializable
data class UserRegisterResponse(
    val requestStatus: String,
    val requestInfo: String,
    val userName: String,
    @Serializable(with = UUIDSerializer::class)
    val key: UUID,
) {
    companion object {
        fun success(userName: String, key: UUID): UserRegisterResponse {
            return UserRegisterResponse(
                requestStatus = "success",
                requestInfo = "",
                userName = userName,
                key = key,
            )
        }

        fun usedUsername(userName: String, key: UUID): UserRegisterResponse {
            return UserRegisterResponse(
                requestStatus = "error",
                requestInfo = "already occupied username",
                userName = userName,
                key = key,
            )
        }

        fun invalidToken(userName: String, key: UUID): UserRegisterResponse {
            return UserRegisterResponse(
                requestStatus = "error",
                requestInfo = "not a valid token",
                userName = userName,
                key = key,
            )
        }
    }
}

class UserRegisterHandler(
    private val tokenManager: TokenManager,
    private val modData: ModData,
) : ParamPostHandler() {
    override val id: ApiID = UserRegisterApiId

    override fun handleRequest(formParams: Map<String, List<String>>, outputStream: OutputStream) {
        try {
            val req = parseUserRequest(formParams)
            if (tokenManager.verify(req.token)) {
                if (!modData.hasUserName(req.userName)) {
                    val user = ImcUser(
                        req.userName,
                        req.token
                    )
                    modData.addUser(user)
                    UserRegisterResponse.success(
                        userName = req.userName,
                        key = tokenManager.addForeverToken().uuid,
                    ).writeToStream(outputStream)
                } else {
                    UserRegisterResponse.usedUsername(
                        userName = req.userName,
                        key = UUID.randomUUID(), // useless
                    ).writeToStream(outputStream)
                }
            } else {
                UserRegisterResponse.invalidToken(
                    userName = req.userName,
                    key = UUID.randomUUID(), // useless
                ).writeToStream(outputStream)
            }
        } catch (e: MissingParameterException) {
            UserRegisterResponse(
                requestStatus = "error",
                requestInfo = e.message ?: "",
                userName = "(error)",
                key = UUID.randomUUID(), // useless
            ).writeToStream(outputStream)
        }
    }
}
