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
import online.ruin_of_future.informative_mc_core.UUIDSerializer
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream
import java.util.*

val PlayerInfoApiId = ApiID("mc-info", "player-info")

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

// For now, we don't implement authentication functionalities.
// So it's a GET handler.
@Suppress("UnUsed")
@Serializable
class PlayerInfo private constructor(
    val players: List<SinglePlayerInfo>,
    override val id: ApiID = PlayerInfoApiId
) : ParamGetHandler() {

    private val server: MinecraftServer
        get() = ImcCore.server

    constructor() : this(emptyList())

    override fun handleRequest(
        queryParamMap: Map<String, List<String>>,
        outputStream: OutputStream
    ) {
        val filteredPlayers = server.playerManager.playerList
            .filter { playerEntity: ServerPlayerEntity? ->
                if (queryParamMap.containsKey("name")) {
                    playerEntity?.name?.asString() == queryParamMap["name"]!![0]
                } else if (queryParamMap.containsKey("uuid")) {
                    playerEntity?.uuid?.toString() == queryParamMap["uuid"]!![0]
                } else {
                    true
                }
            }.mapNotNull { playerEntity: ServerPlayerEntity? ->
                when (playerEntity) {
                    null -> null
                    else -> SinglePlayerInfo(playerEntity)
                }
            }
        val info = PlayerInfo(filteredPlayers)
        info.writeToStream(outputStream)
    }
}