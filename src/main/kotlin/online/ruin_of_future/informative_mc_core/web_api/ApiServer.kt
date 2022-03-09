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
import online.ruin_of_future.informative_mc_core.ModEntryPoint
import online.ruin_of_future.informative_mc_core.web_api.handler.*
import org.apache.logging.log4j.LogManager
import java.io.ByteArrayOutputStream

/**
 * Server which expose web api and potential web pages.
 * */
@OptIn(ExperimentalSerializationApi::class)
object ApiServer {
    private val LOGGER = LogManager.getLogger("IMC-Core")

    private val serverPort: Int
        get() = ModEntryPoint.config.port

    private lateinit var app: Javalin

    private val paraFreeApiHandlers = mutableMapOf<ApiID, ParaFreeApiHandler>()

    fun registerApiHandler(apiHandler: ParaFreeApiHandler) {
        paraFreeApiHandlers.putIfAbsent(apiHandler.id, apiHandler)
    }

    private fun setupAllApi() {
        registerApiHandler(Heartbeat())
        registerApiHandler(JvmInfo())
        registerApiHandler(OSInfo())
        registerApiHandler(PlayerInfo())

        // Late init
        paraFreeApiHandlers.forEach { (_, handler) ->
            handler.setup()
        }
    }

    /**
     * Late init function, called when mod loaded
     * */
    fun setup() {
        setupAllApi()

        app = Javalin.create { config ->
            config.enableCorsForAllOrigins()
        }.routes {
            path("api") {
                paraFreeApiHandlers.forEach { (id, handler) ->
                    get(id.toURIString()) { ctx ->
                        val outputStream = ByteArrayOutputStream()
                        handler.handleRequest(outputStream)
                        ctx.result(outputStream.toByteArray())
                    }
                }
            }
        }.start(serverPort)
        LOGGER.info("IMC-Core is starting at port $serverPort")
    }
}