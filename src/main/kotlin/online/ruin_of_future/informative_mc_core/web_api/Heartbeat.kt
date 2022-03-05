package online.ruin_of_future.informative_mc_core.web_api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.OutputStream

val HeartbeatApiId = ApiID("system-info", "heartbeat")

@Serializable
@OptIn(ExperimentalSerializationApi::class)
class Heartbeat private constructor(
    val status: String,
    override val id: ApiID = HeartbeatApiId,
) : ParaFreeApiHandler() {

    constructor(id: ApiID = HeartbeatApiId) : this("debug", id)

    override fun handle(outputStream: OutputStream) {
        val info = Heartbeat("healthy")
        Json.encodeToStream(info, outputStream)
    }
}