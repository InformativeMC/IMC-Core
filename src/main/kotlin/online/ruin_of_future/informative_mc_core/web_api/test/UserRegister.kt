package online.ruin_of_future.informative_mc_core.web_api.test

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import online.ruin_of_future.informative_mc_core.auth.TokenManager
import online.ruin_of_future.informative_mc_core.util.generateRandomString
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import online.ruin_of_future.informative_mc_core.web_api.id.UserRegisterApiId
import online.ruin_of_future.informative_mc_core.web_api.response.UserRegisterResponse
import java.util.concurrent.TimeUnit

class UserRegisterTest(
    private val tmpAuthManager: TokenManager
) : ApiTest() {
    override val apiId: ApiId = UserRegisterApiId

    override fun run(): Boolean {
        val username = "TEST_${generateRandomString(5)}"
        val token = tmpAuthManager.addTimedOnceToken(TimeUnit.MINUTES.toMillis(10))
        val formBody = FormBody
            .Builder()
            .add("username", username)
            .add("userToken", token.uuid.toString())
            .build()
        val request = Request
            .Builder()
            .url(apiAddress)
            .post(formBody)
            .build()
        val response = client.newCall(request).execute()
        return if (response.code != 200) {
            false
        } else {
            // Exceptions will be thrown out. Do worry them here.
            val body = Json.decodeFromString<UserRegisterResponse>(response.body!!.string())
            body.requestStatus == "success" && body.responseBody?.userName == username
        }
    }
}