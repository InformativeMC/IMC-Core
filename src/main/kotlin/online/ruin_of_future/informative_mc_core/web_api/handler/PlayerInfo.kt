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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import online.ruin_of_future.informative_mc_core.ModEntryPoint
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream
import java.util.*

val PlayerInfoApiId = ApiID("mc-info", "player-info")

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}

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
@OptIn(ExperimentalSerializationApi::class)
@Serializable
class PlayerInfo private constructor(
    val players: List<SinglePlayerInfo>,
    override val id: ApiID = PlayerInfoApiId
) : ParamGetHandler() {

    private val server: MinecraftServer
        get() = ModEntryPoint.server

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
        Json.encodeToStream(info, outputStream)
    }
}