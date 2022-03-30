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
import online.ruin_of_future.informative_mc_core.web_api.id.UserTestApiId
import online.ruin_of_future.informative_mc_core.web_api.response.UserTestResponse
import online.ruin_of_future.informative_mc_core.web_api.response.UserTestResponseDetail
import java.io.OutputStream

class UserTestHandler(
    private val modDataManager: ModDataManager,
) : ParamPostHandler() {
    override val id: ApiId = UserTestApiId

    override fun handleRequest(formParams: Map<String, List<String>>, outputStream: OutputStream) {
        val req = parseUserRequest(formParams)
        val res = if (!modDataManager.userManager.hasUsername(req.username)) {
            UserTestResponse.usernameError(req.username)
        } else if (!modDataManager.userManager.verifyUserToken(req.username, req.token)) {
            UserTestResponse.invalidTokenError(req.token)
        } else {
            UserTestResponse.success(UserTestResponseDetail(req.username))
        }
        res.writeToStream(outputStream)
    }
}