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
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import online.ruin_of_future.informative_mc_core.core.ImcCore
import online.ruin_of_future.informative_mc_core.data.ModData
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import online.ruin_of_future.informative_mc_core.web_api.id.PlayerStatApiId
import online.ruin_of_future.informative_mc_core.web_api.response.PlayerStatResponse
import online.ruin_of_future.informative_mc_core.web_api.response.PlayerStatResponseDetail
import online.ruin_of_future.informative_mc_core.web_api.response.SinglePlayerStat
import java.io.OutputStream

class PlayerStatHandler(
    private val modData: ModData,
) : ParamPostHandler() {
    override val id: ApiId = PlayerStatApiId

    private val server: MinecraftServer
        get() = ImcCore.server

    private inline fun <reified T> Array<String>.parseKthNum(k: Int): T {
        return if (this.size < k) {
            throw IndexOutOfBoundsException("API argument number mismatches!!!")
        } else {
            val v = this[k]
            when (T::class) {
                Short::class -> v.toShort() as T
                Int::class -> v.toInt() as T
                Long::class -> v.toLong() as T
                Float::class -> v.toFloat() as T
                Double::class -> v.toDouble() as T
                else -> throw TypeCastException("Cannot parse $v as type ${T::class.simpleName}")
            }
        }
    }

    private val opMap = mapOf<String, (ServerPlayerEntity, Array<String>) -> Unit>(
        "watch" to { _, _ -> },
        "damage" to { playerEntity, arg ->
            playerEntity.sendMessage(LiteralText("IMC is damaging you!"), true)
            playerEntity.damage(DamageSource.LIGHTNING_BOLT, arg.parseKthNum(0))
        },
        "heal" to { playerEntity, arg ->
            playerEntity.sendMessage(LiteralText("IMC is healing you!"), true)
            playerEntity.heal(arg.parseKthNum(0))
        },
        "feed" to { playerEntity, arg ->
            playerEntity.sendMessage(LiteralText("IMC is feeding you!"), true)
            playerEntity.hungerManager.add(arg.parseKthNum(0), arg.parseKthNum(1))
        },
    )

    override fun handleRequest(
        formParams: Map<String, List<String>>,
        outputStream: OutputStream
    ) {
        val req = parseUserRequest(formParams)
        val res = if (!modData.userManager.hasUserName(req.username)) {
            PlayerStatResponse.usernameError(req.username)
        } else if (!modData.userManager.verifyUserToken(req.username, req.token)) {
            PlayerStatResponse.invalidTokenError(req.token)
        } else {
            val target = formParams["target"]?.get(0)
                ?.let { Json.decodeFromString<Array<String>>(it) }
                ?.toSet()
                ?: setOf()
            val op = formParams["operation"]?.get(0)
                ?.let { opMap[it] }
            val arg = formParams["arg"]?.get(0)
                ?.let { Json.decodeFromString<Array<String>>(it) }
                ?: arrayOf()
            if (op == null) {
                PlayerStatResponse.invalidOpError(formParams["operation"]?.get(0))
            } else {
                try {
                    val filteredPlayers = server.playerManager.playerList
                        .filterNotNull()
                        .filter { playerEntity: ServerPlayerEntity ->
                            if (target.isEmpty()) {
                                true
                            } else {
                                playerEntity.name.asString() in target
                            }
                        }.map { playerEntity: ServerPlayerEntity ->
                            op(playerEntity, arg)
                            SinglePlayerStat(playerEntity)
                        }
                    PlayerStatResponse.success(PlayerStatResponseDetail(filteredPlayers))
                } catch (e: Throwable) {
                    e.printStackTrace()
                    PlayerStatResponse.error(e.message ?: "internal error while executing operation.")
                }
            }
        }
        res.writeToStream(outputStream)
    }
}