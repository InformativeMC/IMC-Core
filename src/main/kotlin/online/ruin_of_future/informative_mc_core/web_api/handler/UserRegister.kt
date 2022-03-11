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
import online.ruin_of_future.informative_mc_core.ImcUser
import online.ruin_of_future.informative_mc_core.ModData
import online.ruin_of_future.informative_mc_core.UUIDSerializer
import online.ruin_of_future.informative_mc_core.token_system.TokenManager
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream
import java.util.*

val UserRegisterApiId = ApiID("imc-manage", "register")

@Serializable
data class UserRegisterResponse(
    val status: String,
    val info: String,
    val userName: String,
    @Serializable(with = UUIDSerializer::class)
    val key: UUID,
)

data class UserRegisterRequest(
    val userName: String,
    val token: UUID,
)

class UserRegisterHandler(
    private val tokenManager: TokenManager,
    private val modData: ModData,
) : ParamPostHandler() {
    override val id: ApiID = UserRegisterApiId

    private fun parseParams(
        formParamMap: Map<String, List<String>>
    ): UserRegisterRequest {
        val u = formParamMap["token"]?.get(0)
            ?: throw MissingParameterException("Need token for register")
        val uuid = try {
            UUID.fromString(u)
        } catch (e: IllegalArgumentException) {
            UUID.randomUUID() // useless
        }
        return UserRegisterRequest(
            userName = formParamMap["userName"]?.get(0)
                ?: throw MissingParameterException("Need user name for register"),
            token = uuid,
        )
    }

    override fun handleRequest(formParamMap: Map<String, List<String>>, outputStream: OutputStream) {
        try {
            val req = parseParams(formParamMap)
            if (tokenManager.validate(req.token)) {
                if (!modData.hasUser(req.userName)) {
                    val user = ImcUser(
                        req.userName,
                        tokenManager.getToken(req.token)!!
                    )
                    modData.addUser(user)
                    UserRegisterResponse(
                        status = "success",
                        info = "",
                        userName = req.userName,
                        key = tokenManager.addForeverToken().uuid,
                    ).writeToStream(outputStream)
                } else {
                    UserRegisterResponse(
                        status = "error",
                        info = "already occupied username",
                        userName = req.userName,
                        key = UUID.randomUUID(), // useless
                    ).writeToStream(outputStream)
                }
            } else {
                UserRegisterResponse(
                    status = "error",
                    info = "not a valid token",
                    userName = req.userName,
                    key = UUID.randomUUID(), // useless
                ).writeToStream(outputStream)
            }
        } catch (e: MissingParameterException) {
            UserRegisterResponse(
                status = "error",
                info = e.message ?: "",
                userName = "(error)",
                key = UUID.randomUUID(), // useless
            ).writeToStream(outputStream)
        }
    }
}
