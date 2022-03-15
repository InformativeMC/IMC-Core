package online.ruin_of_future.informative_mc_core.web_api.handler

import online.ruin_of_future.informative_mc_core.auth.TokenManager
import online.ruin_of_future.informative_mc_core.data.ModData
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import online.ruin_of_future.informative_mc_core.web_api.response.UserTestResponse
import java.io.OutputStream

val UserTestApiId = ApiID("imc-manage", "test-user")

class UserTestHandler(
    private val tokenManager: TokenManager,
    private val modData: ModData,
) : ParamPostHandler() {
    override val id: ApiID = UserTestApiId

    override fun handleRequest(formParams: Map<String, List<String>>, outputStream: OutputStream) {
        val req = parseUserRequest(formParams)
        if (!modData.hasUserName(req.userName)) {
            UserTestResponse.unknownUserNameError(req.userName).writeToStream(outputStream)
        } else if (!tokenManager.verify(req.token)) {
            UserTestResponse.invalidTokenError(req.userName).writeToStream(outputStream)
        } else {
            UserTestResponse.success(req.userName).writeToStream(outputStream)
        }
    }
}