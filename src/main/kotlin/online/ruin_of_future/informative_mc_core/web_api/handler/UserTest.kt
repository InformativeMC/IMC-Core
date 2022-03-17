package online.ruin_of_future.informative_mc_core.web_api.handler

import online.ruin_of_future.informative_mc_core.data.ModDataManager
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import online.ruin_of_future.informative_mc_core.web_api.response.UserTestResponse
import java.io.OutputStream

val UserTestApiId = ApiID("imc-manage", "test-user")

class UserTestHandler(
    private val modDataManager: ModDataManager,
) : ParamPostHandler() {
    override val id: ApiID = UserTestApiId

    override fun handleRequest(formParams: Map<String, List<String>>, outputStream: OutputStream) {
        val req = parseUserRequest(formParams)
        if (!modDataManager.userManager.hasUserName(req.userName)) {
            UserTestResponse.unknownUserNameError(req.userName).writeToStream(outputStream)
        } else if (!modDataManager.tmpAuthManager.verifyToken(req.token)) {
            UserTestResponse.invalidTokenError(req.token).writeToStream(outputStream)
        } else {
            UserTestResponse.success(req.userName).writeToStream(outputStream)
        }
    }
}