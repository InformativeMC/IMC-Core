package online.ruin_of_future.informative_mc_core.web_api.test

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Request
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import online.ruin_of_future.informative_mc_core.web_api.id.HeartbeatApiId
import online.ruin_of_future.informative_mc_core.web_api.response.ApiResponse
import online.ruin_of_future.informative_mc_core.web_api.response.HeartbeatResponse

private class HeartbeatTest : ApiTest<HeartbeatResponse>() {
    override val apiId = HeartbeatApiId

    override suspend fun runWithCallback(
        onSuccess: (response: HeartbeatResponse) -> Unit,
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
                onSuccess(body)
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}

class HeartbeatTestBatch : ApiTestBatch {
    override val name = "Heartbeat"

    private val test = HeartbeatTest()
    override val passedTest = mutableMapOf<ApiId, ApiResponse<*>>()
    override val failedTest = mutableMapOf<ApiId, Throwable>()

    override suspend fun runWithCallback(
        onSuccess: (passedTest: Map<ApiId, ApiResponse<*>>) -> Unit,
        onFailure: (failedTest: Map<ApiId, Throwable>) -> Unit
    ) {
        test.runWithCallback(
            onSuccess = {
                passedTest[test.apiId] = it
            },
            onFailure = {
                failedTest[test.apiId] = it
            }
        )
        onSuccess(passedTest.toMap())
        onFailure(failedTest.toMap())
    }
}