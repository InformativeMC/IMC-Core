package online.ruin_of_future.informative_mc_core.web_api

import kotlinx.serialization.Serializable

@Serializable
data class HeartbeatResponse(val status: String) {
    companion object {
        val HEALTHY = HeartbeatResponse("healthy")
    }
}

/**
 * A sample API which indicates server health.
 * */
class Heartbeat : ApiBase<Nothing, Nothing, HeartbeatResponse>() {
    override val id: ApiID
        get() = ApiID("SystemInfo", "HeatBeat")

    override operator fun invoke(args: Map<Nothing, Nothing>?): HeartbeatResponse {
        // Returning true means everything is OK.
        return HeartbeatResponse.HEALTHY
    }
}