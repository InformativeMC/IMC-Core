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
package online.ruin_of_future.informative_mc_core.web_api

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import kotlinx.serialization.ExperimentalSerializationApi
import net.minecraft.server.MinecraftServer
import online.ruin_of_future.informative_mc_core.config.ModConfig
import online.ruin_of_future.informative_mc_core.data.ModDataManager
import online.ruin_of_future.informative_mc_core.web_api.handler.*
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import org.apache.logging.log4j.LogManager
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import java.io.ByteArrayOutputStream


/**
 * Server which expose web api and potential web pages.
 * */
@Suppress("UnUsed")
@OptIn(ExperimentalSerializationApi::class)
class ApiServer(
    private val mcServer: MinecraftServer,
    private val serverPort: Int,
    modConfig: ModConfig,
    private val modDataManager: ModDataManager,
) : KeyStoreManager(modConfig) {
    private val LOGGER = LogManager.getLogger("IMC-Core")

    private var app: Javalin

    private val paramFreeHandlers = mutableMapOf<ApiId, ParamFreeHandler>()
    private val paramGetHandlers = mutableMapOf<ApiId, ParamGetHandler>()
    private val paramPostHandlers = mutableMapOf<ApiId, ParamPostHandler>()

    private fun registerApiHandler(apiHandler: ParamFreeHandler) {
        paramFreeHandlers.putIfAbsent(apiHandler.id, apiHandler)
    }

    private fun registerApiHandler(apiHandler: ParamGetHandler) {
        paramGetHandlers.putIfAbsent(apiHandler.id, apiHandler)
    }

    private fun registerApiHandler(apiHandler: ParamPostHandler) {
        paramPostHandlers.putIfAbsent(apiHandler.id, apiHandler)
    }

    private fun setupAllApi() {
        registerApiHandler(HeartbeatHandler())
        registerApiHandler(JvmInfoHandler(modDataManager))
        registerApiHandler(OSInfoHandler(modDataManager))
        registerApiHandler(PlayerStatHandler(mcServer, modDataManager))
        registerApiHandler(UserRegisterHandler(modDataManager))
        registerApiHandler(UserTestHandler(modDataManager))
        registerApiHandler(GameMessageHandler(mcServer, modDataManager))

        // Late init
        paramFreeHandlers.forEach { (_, handler) ->
            handler.setup()
        }
        paramGetHandlers.forEach { (_, handler) ->
            handler.setup()
        }
        paramPostHandlers.forEach { (_, handler) ->
            handler.setup()
        }
    }

    init {
        setupAllApi()
        ensureKeyStore()

        app = Javalin.create { config ->
            config.enableCorsForAllOrigins()
            config.enforceSsl = true
            config.server {
                val server = Server()
                val sslConnector = ServerConnector(server, getSslContextFactory())
                sslConnector.port = serverPort
                server.connectors = arrayOf(sslConnector)
                server
            }
        }.routes {
            path("api") {
                paramFreeHandlers.forEach { (id, handler) ->
                    get(id.toURIString()) { ctx ->
                        val outputStream = ByteArrayOutputStream()
                        ctx.contentType("application/json")
                        handler.handleRequest(outputStream)
                        ctx.result(outputStream.toByteArray())
                        outputStream.close()
                    }
                }
                paramGetHandlers.forEach { (id, handler) ->
                    get(id.toURIString()) { ctx ->
                        val outputStream = ByteArrayOutputStream()
                        ctx.contentType("application/json")
                        handler.handleRequest(ctx.queryParamMap(), outputStream)
                        ctx.result(outputStream.toByteArray())
                        outputStream.close()
                    }
                }
                paramPostHandlers.forEach { (id, handler) ->
                    post(id.toURIString()) { ctx ->
                        val outputStream = ByteArrayOutputStream()
                        ctx.contentType("application/json")
                        handler.handleRequest(ctx.formParamMap(), outputStream)
                        ctx.result(outputStream.toByteArray())
                        outputStream.close()
                    }
                }
            }
        }.start()
        LOGGER.info("IMC-Core is starting at port $serverPort")
    }
}