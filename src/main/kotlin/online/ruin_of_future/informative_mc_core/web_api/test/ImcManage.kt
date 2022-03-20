package online.ruin_of_future.informative_mc_core.web_api.test

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
) : ApiTest<UserRegisterResponse> by PostApiTestImpl(
    UserRegisterApiId,
    username,
    tmpAuthUUID,
    UserRegisterResponse.serializer()
) {
    override fun checkResponse(response: UserRegisterResponse) {
        super.checkResponse(response)
        assert(response.responseDetail?.userName == username)
    }
}

private class UserTestTest(
    private val username: String,
    private val tokenUUID: UUID,
) : ApiTest<UserTestResponse> by PostApiTestImpl(
    UserTestApiId,
    username,
    tokenUUID,
    UserTestResponse.serializer()
) {
    override fun checkResponse(response: UserTestResponse) {
        super.checkResponse(response)
        assert(response.responseDetail?.username == username)
    }
}

class ImcManageTestBatch(
    private val tmpAuthUUID: UUID
) : ApiTestBatch {
    override val name = "IMC Manage"
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