package online.ruin_of_future.informative_mc_core.web_api.handler

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream

val HeartbeatApiId = ApiID("system-info", "heartbeat")

@Suppress("UnUsed")
@Serializable
class Heartbeat private constructor(
    val status: String,
    override val id: ApiID = HeartbeatApiId,
) : ParamFreeHandler() {

    constructor() : this("???")

    override fun handleRequest(outputStream: OutputStream) {
        Heartbeat("healthy").writeToStream(outputStream)
    }
}