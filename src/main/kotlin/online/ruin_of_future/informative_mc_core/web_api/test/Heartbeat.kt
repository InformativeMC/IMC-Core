package online.ruin_of_future.informative_mc_core.web_api.test

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Request
import online.ruin_of_future.informative_mc_core.web_api.id.HeartbeatApiId
import online.ruin_of_future.informative_mc_core.web_api.response.HeartbeatResponse

class HeartbeatTest : ApiTest() {
    override val apiId = HeartbeatApiId

    override suspend fun runWithCallback(
        onSuccess: () -> Unit,
        onFailure: (cause: Throwable) -> Unit,
    ) {
        val request = Request
            .Builder()
            .url(apiAddress)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.code != 200) {
                assert(false)
            } else {
                val body = Json.decodeFromString<HeartbeatResponse>(response.body!!.string())
                assert(body.requestStatus == "success")
                assert(body.responseDetail?.status == "healthy")
            }
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}