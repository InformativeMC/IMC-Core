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

    private val paramFreeHandlers = mutableMapOf<ApiID, ParamFreeHandler>()
    private val paramGetHandlers = mutableMapOf<ApiID, ParamGetHandler>()

    fun registerApiHandler(apiHandler: ParamFreeHandler) {
        paramFreeHandlers.putIfAbsent(apiHandler.id, apiHandler)
    }

    fun registerApiHandler(apiHandler: ParamGetHandler) {
        paramGetHandlers.putIfAbsent(apiHandler.id, apiHandler)
    }

    private fun setupAllApi() {
        registerApiHandler(Heartbeat())
        registerApiHandler(JvmInfo())
        registerApiHandler(OSInfo())
        registerApiHandler(PlayerInfo())

        // Late init
        paramFreeHandlers.forEach { (_, handler) ->
            handler.setup()
        }
        paramGetHandlers.forEach { (_, handler) ->
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
                paramFreeHandlers.forEach { (id, handler) ->
                    get(id.toURIString()) { ctx ->
                        val outputStream = ByteArrayOutputStream()
                        handler.handleRequest(outputStream)
                        ctx.result(outputStream.toByteArray())
                    }
                }
                paramGetHandlers.forEach { (id, handler) ->
                    get(id.toURIString()) { ctx ->
                        val outputStream = ByteArrayOutputStream()
                        handler.handleRequest(ctx.queryParamMap(), outputStream)
                        ctx.result(outputStream.toByteArray())
                    }
                }
            }
        }.start(serverPort)
        LOGGER.info("IMC-Core is starting at port $serverPort")
    }
}