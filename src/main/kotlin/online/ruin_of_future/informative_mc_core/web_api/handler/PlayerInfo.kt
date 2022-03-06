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

@Suppress("UnUsed")
@OptIn(ExperimentalSerializationApi::class)
@Serializable
class PlayerInfo private constructor(
    val players: List<SinglePlayerInfo>,
    override val id: ApiID = PlayerInfoApiId
) : ParamFreeHandler() {

    private val server: MinecraftServer
        get() = ModEntryPoint.server

    constructor() : this(emptyList())

    override fun handleRequest(outputStream: OutputStream) {
        val serverPlayers = server.playerManager.playerList.mapNotNull { playerEntity: ServerPlayerEntity? ->
            when (playerEntity) {
                null -> null
                else -> SinglePlayerInfo(playerEntity)
            }
        }
        val info = PlayerInfo(serverPlayers)
        Json.encodeToStream(info, outputStream)
    }
}