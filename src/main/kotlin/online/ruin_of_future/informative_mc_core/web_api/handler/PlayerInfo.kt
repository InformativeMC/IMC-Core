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

import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import online.ruin_of_future.informative_mc_core.ImcCore
import online.ruin_of_future.informative_mc_core.data.ModDataManager
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import online.ruin_of_future.informative_mc_core.web_api.id.PlayerInfoApiId
import online.ruin_of_future.informative_mc_core.web_api.response.PlayerInfoResponse
import online.ruin_of_future.informative_mc_core.web_api.response.PlayerInfoResponseBody
import online.ruin_of_future.informative_mc_core.web_api.response.SinglePlayerInfo
import java.io.OutputStream

class PlayerInfoHandler(
    private val modDataManager: ModDataManager,
) : ParamPostHandler() {
    override val id: ApiId = PlayerInfoApiId

    private val server: MinecraftServer
        get() = ImcCore.server

    override fun handleRequest(
        formParams: Map<String, List<String>>,
        outputStream: OutputStream
    ) {
        val req = parseUserRequest(formParams)
        val res = if (!modDataManager.userManager.hasUserName(req.userName)) {
            PlayerInfoResponse.usernameError(req.userName)
        } else if (!modDataManager.userManager.verifyUserToken(req.userName, req.token)) {
            PlayerInfoResponse.invalidTokenError(req.token)
        } else {
            val filteredPlayers = server.playerManager.playerList
                .filter { playerEntity: ServerPlayerEntity? ->
                    if (formParams.containsKey("name")) {
                        playerEntity?.name?.asString() == formParams["name"]!![0]
                    } else if (formParams.containsKey("uuid")) {
                        playerEntity?.uuid?.toString() == formParams["uuid"]!![0]
                    } else {
                        true
                    }
                }.mapNotNull { playerEntity: ServerPlayerEntity? ->
                    when (playerEntity) {
                        null -> null
                        else -> SinglePlayerInfo(playerEntity)
                    }
                }
            PlayerInfoResponse.success(PlayerInfoResponseBody(filteredPlayers))
        }
        res.writeToStream(outputStream)
    }
}