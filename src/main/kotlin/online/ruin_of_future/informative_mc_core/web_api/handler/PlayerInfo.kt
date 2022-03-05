package online.ruin_of_future.informative_mc_core.web_api.handler

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import online.ruin_of_future.informative_mc_core.ModEntryPoint
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream

val PlayerInfoApiId = ApiID("mc-info", "player-info")

@OptIn(ExperimentalSerializationApi::class)
@Serializable
class PlayerInfo private constructor(
    val players: List<String>,
    override val id: ApiID = PlayerInfoApiId
) : ParaFreeApiHandler() {

    private val server: MinecraftServer
        get() = ModEntryPoint.server

    constructor() : this(listOf())

    override fun handleRequest(outputStream: OutputStream) {
        val serverPlayers = server.playerManager.playerList.mapNotNull { playerEntity: ServerPlayerEntity? ->
            playerEntity?.name.toString()
        }
        val info = PlayerInfo(serverPlayers)
        Json.encodeToStream(info, outputStream)
    }
}