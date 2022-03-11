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
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import online.ruin_of_future.informative_mc_core.ImcCore
import online.ruin_of_future.informative_mc_core.ModData
import online.ruin_of_future.informative_mc_core.UUIDSerializer
import online.ruin_of_future.informative_mc_core.token_system.TokenManager
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream
import java.util.*

val PlayerInfoApiId = ApiID("mc-info", "player-info")

// TODO: Lift `requestStatus` and `requestInfo` out.

@Suppress("UnUsed")
@Serializable
class SinglePlayerInfo private constructor(
    val name: String,
    val entityName: String,
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    val health: Float,
    val foodLevel: Int,
    val experienceLevel: Int,
) {
    constructor(playerEntity: ServerPlayerEntity) : this(
        name = playerEntity.name.asString(),
        entityName = playerEntity.entityName,
        uuid = playerEntity.uuid,
        health = playerEntity.health,
        foodLevel = playerEntity.hungerManager.foodLevel,
        experienceLevel = playerEntity.experienceLevel
    )
}

@Suppress("UnUsed")
@Serializable
class PlayerInfoResponse(
    val requestStatus: String,
    val requestInfo: String,
    val players: List<SinglePlayerInfo>,
) {
    companion object {
        fun unknownUser(userName: String): PlayerInfoResponse {
            return PlayerInfoResponse(
                requestStatus = "error",
                requestInfo = "unknown user: $userName",
                players = listOf(),
            )
        }

        fun invalidToken(): PlayerInfoResponse {
            return PlayerInfoResponse(
                requestStatus = "error",
                requestInfo = "invalid token",
                players = listOf(),
            )
        }


        fun success(players: List<SinglePlayerInfo>): PlayerInfoResponse {
            return PlayerInfoResponse(
                requestStatus = "success",
                requestInfo = "",
                players = players,
            )
        }
    }
}

class PlayerInfoHandler(
    private val tokenManager: TokenManager,
    private val modData: ModData,
) : ParamPostHandler() {
    override val id: ApiID = PlayerInfoApiId

    private val server: MinecraftServer
        get() = ImcCore.server

    override fun handleRequest(
        formParams: Map<String, List<String>>,
        outputStream: OutputStream
    ) {
        val req = parseUserRequest(formParams)
        if (!modData.hasUserName(req.userName)) {
            PlayerInfoResponse.unknownUser(req.userName).writeToStream(outputStream)
        } else if (!tokenManager.verify(req.token)) {
            PlayerInfoResponse.invalidToken().writeToStream(outputStream)
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
            PlayerInfoResponse.success(filteredPlayers).writeToStream(outputStream)
        }
    }
}