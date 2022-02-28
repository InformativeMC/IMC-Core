package online.ruin_of_future.informative_mc_core.web_api

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.ContentType
import io.javalin.http.Context
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import online.ruin_of_future.informative_mc_core.ModEntryPoint
import org.apache.logging.log4j.LogManager

/**
 * Server which expose web api and potential web pages.
 * */
@OptIn(ExperimentalSerializationApi::class)
object Server {
    private val LOGGER = LogManager.getLogger("IMC-Core")

    private val serverPort: Int
        get() = ModEntryPoint.config.port

    private lateinit var app: Javalin


    /**
     * Context.json requires extra library. Use kotlinx.serialization instead.
     * */
    private inline fun <reified T> Context.jsonResult(serializable: T): Context {
        return contentType(ContentType.APPLICATION_JSON).result(Json.encodeToString(serializable))
    }

    /**
     * Late init function, called when mod loaded
     * */
    fun setup() {
        app = Javalin.create().routes {
            path("api") {
                get(HeartbeatApiId.toURIString()) { ctx ->
                    ctx.jsonResult(Heartbeat.handle())
                }
                get(JvmInfoApiID.toURIString()) { ctx ->
                    ctx.jsonResult(JvmInfo.handle())
                }
                get(OSInfoApiId.toURIString()) { ctx ->
                    ctx.jsonResult(OSInfo.handle())
                }
            }
        }.start(serverPort)
        LOGGER.info("IMC-Core is starting at port $serverPort")
    }
}