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
package online.ruin_of_future.informative_mc_core.web_api.response

import kotlinx.serialization.Serializable
import net.minecraft.server.network.ServerPlayerEntity
import online.ruin_of_future.informative_mc_core.util.UUIDSerializer
import java.util.*

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

@Serializable
class PlayerInfoResponseBody(
    val players: List<SinglePlayerInfo>,
)

@Suppress("UnUsed")
@Serializable
class PlayerInfoResponse(
    override val requestStatus: String,
    override val requestInfo: String,
    override val responseBody: PlayerInfoResponseBody?
) : ApiResponse<PlayerInfoResponseBody>() {

    companion object CommonResponses : ApiAuthCommonResponses<PlayerInfoResponseBody, PlayerInfoResponse>(
        responseBuilder = { status, info, body -> PlayerInfoResponse(status, info, body) }
    )
}