package online.ruin_of_future.informative_mc_core.web_api.response

import kotlinx.serialization.Serializable

@Serializable
class HeartbeatResponseBody(
    val status: String,
)

@Serializable
class HeartbeatResponse(
    override val requestStatus: String,
    override val requestInfo: String,
    override val responseBody: HeartbeatResponseBody?
) : ApiResponse<HeartbeatResponseBody?>() {

    companion object {
        val HEALTHY = HeartbeatResponse(
            requestStatus = "success",
            requestInfo = "",
            responseBody = HeartbeatResponseBody("healthy"),
        )
    }
}