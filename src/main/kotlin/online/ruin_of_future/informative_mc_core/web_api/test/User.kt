package online.ruin_of_future.informative_mc_core.web_api.test

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import online.ruin_of_future.informative_mc_core.util.generateRandomString
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import online.ruin_of_future.informative_mc_core.web_api.id.UserRegisterApiId
import online.ruin_of_future.informative_mc_core.web_api.id.UserTestApiId
import online.ruin_of_future.informative_mc_core.web_api.response.ApiResponse
import online.ruin_of_future.informative_mc_core.web_api.response.UserRegisterResponse
import online.ruin_of_future.informative_mc_core.web_api.response.UserTestResponse
import java.util.*

private class UserRegisterTest(
    private val tmpAuthUUID: UUID,
    private val username: String,
) : ApiTest<UserRegisterResponse>() {
    override val apiId: ApiId = UserRegisterApiId

    override suspend fun runWithCallback(
        onSuccess: (response: UserRegisterResponse) -> Unit,
        onFailure: (cause: Throwable) -> Unit,
    ) {
        val formBody = FormBody
            .Builder()
            .add("username", username)
            .add("token", tmpAuthUUID.toString())
            .build()
        val request = Request
            .Builder()
            .url(apiAddress)
            .post(formBody)
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.code != 200) {
                assert(false)
            } else {
                val body = Json.decodeFromString<UserRegisterResponse>(response.body!!.string())
                assert(body.requestStatus == "success")
                assert(body.responseDetail?.userName == username)
                assert(body.responseDetail?.uuid != null)
                onSuccess(body)
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}

private class UserTestTest(
    private val username: String,
    private val tokenUUID: UUID,
) : ApiTest<UserTestResponse>() {
    override val apiId: ApiId = UserTestApiId

    override suspend fun runWithCallback(
        onSuccess: (response: UserTestResponse) -> Unit,
        onFailure: (cause: Throwable) -> Unit
    ) {
        val formBody = FormBody
            .Builder()
            .add("username", username)
            .add("token", tokenUUID.toString())
            .build()
        val request = Request
            .Builder()
            .post(formBody)
            .url(apiAddress)
            .build()
        val response = client.newCall(request).execute()
        try {
            if (response.code != 200) {
                assert(false)
            } else {
                val body = Json.decodeFromString<UserTestResponse>(response.body!!.string())
                assert(body.requestStatus == "success")
                assert(body.responseDetail?.username == username)
                onSuccess(body)
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}

class UserTestBatch(
    private val tmpAuthUUID: UUID
) : ApiTestBatch {
    override val name = "User Test"
    override val passedTest = mutableMapOf<ApiId, ApiResponse<*>>()
    override val failedTest = mutableMapOf<ApiId, Throwable>()

    private val username = "TEST_${generateRandomString(5)}"
    private lateinit var tokenUUID: UUID
    override suspend fun runWithCallback(
        onSuccess: (passedTest: Map<ApiId, ApiResponse<*>>) -> Unit,
        onFailure: (failedTest: Map<ApiId, Throwable>) -> Unit
    ) {
        UserRegisterTest(tmpAuthUUID, username).runWithCallback(
            onSuccess = {
                passedTest[UserRegisterApiId] = it
                tokenUUID = it.responseDetail?.uuid!!
            },
            onFailure = {
                failedTest[UserRegisterApiId] = it
            },
        )
        UserTestTest(username, tokenUUID).runWithCallback(
            onSuccess = {
                passedTest[UserTestApiId] = it
            },
            onFailure = {
                failedTest[UserTestApiId] = it
            },
        )
        onSuccess(passedTest)
        onFailure(failedTest)
    }
}