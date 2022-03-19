package online.ruin_of_future.informative_mc_core.web_api.handler

import online.ruin_of_future.informative_mc_core.data.ModDataManager
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import online.ruin_of_future.informative_mc_core.web_api.response.UserTestResponse
import online.ruin_of_future.informative_mc_core.web_api.response.UserTestResponseBody
import java.io.OutputStream

val UserTestApiId = ApiID("imc-manage", "test-user")

class UserTestHandler(
    private val modDataManager: ModDataManager,
) : ParamPostHandler() {
    override val id: ApiID = UserTestApiId

    override fun handleRequest(formParams: Map<String, List<String>>, outputStream: OutputStream) {
        val req = parseUserRequest(formParams)
        val res = if (!modDataManager.userManager.hasUserName(req.userName)) {
            UserTestResponse.usernameError(req.userName)
        } else if (!modDataManager.userManager.verifyUserToken(req.userName, req.token)) {
            UserTestResponse.invalidTokenError(req.token)
        } else {
            UserTestResponse.success(UserTestResponseBody(req.userName))
        }
        res.writeToStream(outputStream)
    }
}