package online.ruin_of_future.informative_mc_core.web_api

import kotlinx.serialization.Serializable

val HeartbeatApiId = ApiID("system-info", "heartbeat")

@Serializable
data class Heartbeat(
    val status: String,
    val id: ApiID = HeartbeatApiId,
) {
    companion object {
        fun handle(): Heartbeat {
            return Heartbeat("healthy")
        }
    }
}