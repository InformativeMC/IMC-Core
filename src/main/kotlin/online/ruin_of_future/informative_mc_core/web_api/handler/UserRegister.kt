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

import online.ruin_of_future.informative_mc_core.auth.TokenManager
import online.ruin_of_future.informative_mc_core.data.ImcUser
import online.ruin_of_future.informative_mc_core.data.ModData
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import online.ruin_of_future.informative_mc_core.web_api.response.UserRegisterResponse
import java.io.OutputStream
import java.util.*

val UserRegisterApiId = ApiID("imc-manage", "register")

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
                    UserRegisterResponse.usedUsernameError(
                        userName = req.userName,
                        key = UUID.randomUUID(), // useless
                    ).writeToStream(outputStream)
                }
            } else {
                UserRegisterResponse.invalidTokenError(
                    userName = req.userName,
                    key = UUID.randomUUID(), // useless
                ).writeToStream(outputStream)
            }
        } catch (e: MissingParameterException) {
            UserRegisterResponse.unknownError().writeToStream(outputStream)
        }
    }
}
