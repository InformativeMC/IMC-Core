package online.ruin_of_future.informative_mc_core.web_api.test

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Request
import online.ruin_of_future.informative_mc_core.web_api.id.HeartbeatApiId
import online.ruin_of_future.informative_mc_core.web_api.response.HeartbeatResponse

class HeartbeatTest : ApiTest() {
    override val apiId = HeartbeatApiId

    override fun run(): Boolean {
        val request = Request
            .Builder()
            .url(apiAddress)
            .build()

        val response = client.newCall(request).execute()
        return if (response.code != 200) {
            false
        } else {
            val body = Json.decodeFromString<HeartbeatResponse>(response.body!!.string())
            body.requestStatus == "success" && body.responseDetail?.status == "healthy"
        }
    }
}