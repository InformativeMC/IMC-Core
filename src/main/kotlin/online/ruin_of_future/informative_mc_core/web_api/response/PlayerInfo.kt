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
) : ApiResponse<PlayerInfoResponseBody?>() {
    companion object {
        fun unknownUserError(userName: String): PlayerInfoResponse {
            return PlayerInfoResponse(
                requestStatus = "error",
                requestInfo = "unknown user: $userName",
                responseBody = null,
            )
        }

        fun invalidTokenError(): PlayerInfoResponse {
            return PlayerInfoResponse(
                requestStatus = "error",
                requestInfo = "invalid token",
                responseBody = null,
            )
        }

        fun success(players: List<SinglePlayerInfo>): PlayerInfoResponse {
            return PlayerInfoResponse(
                requestStatus = "success",
                requestInfo = "",
                responseBody = PlayerInfoResponseBody(players)
            )
        }
    }
}