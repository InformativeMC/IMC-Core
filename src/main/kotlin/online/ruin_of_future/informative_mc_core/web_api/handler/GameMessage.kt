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
import net.minecraft.text.LiteralText
import online.ruin_of_future.informative_mc_core.data.ModDataManager
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import online.ruin_of_future.informative_mc_core.web_api.id.GameMessageApiId
import online.ruin_of_future.informative_mc_core.web_api.response.GameMessageResponse
import online.ruin_of_future.informative_mc_core.web_api.response.GameMessageResponseDetail
import java.io.OutputStream

class GameMessageHandler(
    private val server: MinecraftServer,
    private val modDataManager: ModDataManager,
) : ParamPostHandler() {
    override val id: ApiId = GameMessageApiId

    // TODO: More place holders for messages.

    override fun handleRequest(formParams: Map<String, List<String>>, outputStream: OutputStream) {
        val req = parseUserRequest(formParams)
        val res = if (!modDataManager.userManager.hasUsername(req.username)) {
            GameMessageResponse.usernameError(req.username)
        } else if (!modDataManager.userManager.verifyUserToken(req.username, req.token)) {
            GameMessageResponse.invalidTokenError(req.token)
        } else {
            try {
                val target = formParams["target"]?.get(0)
                    ?.let { Json.decodeFromString<Array<String>>(it) }
                    ?.toSet()
                    ?: throw IllegalArgumentException("Need \"target\" parameter")
                val message = formParams["message"]?.get(0)
                    ?: throw IllegalArgumentException("Need \"message\" parameter")
                val actionBar = formParams["actionBar"]?.get(0)
                    ?.let { Json.decodeFromString<Boolean>(it) }
                    ?: throw IllegalArgumentException("Need \"actionBar\" parameter")
                server.playerManager.playerList
                    .filterNotNull()
                    .forEach {
                        if (it.name.asString() in target) {
                            it.sendMessage(LiteralText(message), actionBar)
                        }
                    }
                GameMessageResponse.success(GameMessageResponseDetail())
            } catch (e: Throwable) {
                GameMessageResponse.error(e.message ?: "unknown internal error during message sending")
            }
        }
        res.writeToStream(outputStream)
    }
}
