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
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import online.ruin_of_future.informative_mc_core.web_api.response.OSInfoResponse
import online.ruin_of_future.informative_mc_core.web_api.response.OSInfoResponseBody
import java.io.OutputStream

val OSInfoApiId = ApiID("system-info", "os-info")

class OSInfoHandler(
    private val modDataManager: ModDataManager,
) : ParamPostHandler() {
    override val id: ApiID = OSInfoApiId
    override fun handleRequest(
        formParams: Map<String, List<String>>,
        outputStream: OutputStream
    ) {
        val req = parseUserRequest(formParams)
        val res = if (!modDataManager.userManager.hasUserName(req.userName)) {
            OSInfoResponse.usernameError(req.userName)
        } else if (!modDataManager.userManager.verifyUserToken(req.userName, req.token)) {
            OSInfoResponse.invalidTokenError(req.token)
        } else {
            OSInfoResponse.success(OSInfoResponseBody.getCurrent())
        }
        res.writeToStream(outputStream)
    }
}