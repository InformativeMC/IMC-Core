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

import online.ruin_of_future.informative_mc_core.data.ModData
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import online.ruin_of_future.informative_mc_core.web_api.id.JvmInfoApiId
import online.ruin_of_future.informative_mc_core.web_api.response.JvmInfoResponse
import online.ruin_of_future.informative_mc_core.web_api.response.JvmInfoResponseDetail
import java.io.OutputStream

class JvmInfoHandler(
    private val modData: ModData,
) : ParamPostHandler() {
    override val id: ApiId = JvmInfoApiId

    override fun handleRequest(
        formParams: Map<String, List<String>>,
        outputStream: OutputStream
    ) {
        val req = parseUserRequest(formParams)
        val res = if (!modData.userManager.hasUser(req.username)) {
            JvmInfoResponse.usernameError(req.username)
        } else if (!modData.userManager.verifyUserToken(req.username, req.token)) {
            JvmInfoResponse.invalidTokenError(req.token)
        } else {
            JvmInfoResponse.success(JvmInfoResponseDetail.getCurrent())
        }
        res.writeToStream(outputStream)
    }
}
