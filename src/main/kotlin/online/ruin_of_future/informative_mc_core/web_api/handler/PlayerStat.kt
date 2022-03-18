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

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import online.ruin_of_future.informative_mc_core.ImcCore
import online.ruin_of_future.informative_mc_core.data.ModDataManager
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import online.ruin_of_future.informative_mc_core.web_api.response.PlayerStatResponse
import online.ruin_of_future.informative_mc_core.web_api.response.PlayerStatResponseBody
import online.ruin_of_future.informative_mc_core.web_api.response.SinglePlayerStat
import java.io.OutputStream
import java.util.*

val PlayerInfoApiId = ApiID("mc-manage", "player-stat")

class PlayerStatHandler(
    private val modDataManager: ModDataManager,
) : ParamPostHandler() {
    override val id: ApiID = PlayerInfoApiId

    private val server: MinecraftServer
        get() = ImcCore.server

    private val base64Decoder = Base64.getDecoder()

    private inline fun <reified T : Number> Array<String>.parseKthNum(k: Int, defaultValue: T): T {
        return if (this.size < k) {
            defaultValue
        } else {
            this[k].toDouble() as T
        }
    }

    private val opMap = mapOf<String, (ServerPlayerEntity, Array<String>) -> Unit>(
        "watch" to { _, _ -> },
        "heal" to { playerEntity, args ->
            playerEntity.heal(args.parseKthNum(0, 0f))
        },
        "feed" to { playerEntity, args ->
            val food = args.parseKthNum(0, 0)
            val saturationModifier = args.parseKthNum(1, 0f)
            playerEntity.hungerManager.add(food, saturationModifier)
        },
        "give" to { playerEntity, args ->
            // TODO:
            // playerEntity.giveItemStack()
        }
    )

    override fun handleRequest(
        formParams: Map<String, List<String>>,
        outputStream: OutputStream
    ) {
        val req = parseUserRequest(formParams)
        val res = if (!modDataManager.userManager.hasUserName(req.userName)) {
            PlayerStatResponse.usernameError(req.userName)
        } else if (!modDataManager.userManager.verifyUserToken(req.userName, req.token)) {
            PlayerStatResponse.invalidTokenError(req.token)
        } else {
            val target = formParams["target"]?.get(0)
                ?.let { base64Decoder.decode(it).toString() }
                ?.let { Json.decodeFromString<Array<String>>(it) }
                ?.toSet()
                ?: setOf()
            val op = formParams["operation"]?.get(0)
                ?.let { Json.decodeFromString<String>(it) }
                ?.let { opMap[it] }
            val args = formParams["arg"]?.get(0)
                ?.let { base64Decoder.decode(it).toString() }
                ?.let { Json.decodeFromString<Array<String>>(it) }
                ?: arrayOf()
            if (op == null) {
                PlayerStatResponse.invalidOpError(formParams["operation"]?.get(0))
            } else {
                val filteredPlayers = server.playerManager.playerList
                    .filterNotNull()
                    .filter { playerEntity: ServerPlayerEntity ->
                        if (target.isEmpty()) {
                            true
                        } else {
                            playerEntity.name.asString() in target
                        }
                    }.map { playerEntity: ServerPlayerEntity ->
                        op(playerEntity, args)
                        SinglePlayerStat(playerEntity)
                    }
                PlayerStatResponse.success(PlayerStatResponseBody(filteredPlayers))
            }
        }
        res.writeToStream(outputStream)
    }
}