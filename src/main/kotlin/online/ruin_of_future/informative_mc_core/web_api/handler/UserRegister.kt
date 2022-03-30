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

import online.ruin_of_future.informative_mc_core.data.ModDataManager
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import online.ruin_of_future.informative_mc_core.web_api.id.UserRegisterApiId
import online.ruin_of_future.informative_mc_core.web_api.response.UserRegisterResponse
import online.ruin_of_future.informative_mc_core.web_api.response.UserRegisterResponseDetail
import java.io.OutputStream

class UserRegisterHandler(
    private val modDataManager: ModDataManager,
) : ParamPostHandler() {
    override val id: ApiId = UserRegisterApiId

    override fun handleRequest(formParams: Map<String, List<String>>, outputStream: OutputStream) {
        try {
            val req = parseUserRequest(formParams)
            val res = if (modDataManager.tmpAuthManager.verifyToken(req.token)) {
                if (!modDataManager.userManager.hasUserName(req.username)) {
                    val user = modDataManager.userManager.addUser(req.username)
                    UserRegisterResponse
                        .success(UserRegisterResponseDetail(user.username, user.userToken.uuid))
                } else {
                    UserRegisterResponse.usernameError(
                        userName = req.username,
                    )
                }
            } else {
                UserRegisterResponse.invalidTokenError(
                    uuid = req.token,
                )
            }
            res.writeToStream(outputStream)
        } catch (e: MissingParameterException) {
            UserRegisterResponse.error(e.message ?: "").writeToStream(outputStream)
        }
    }
}
