package online.ruin_of_future.informative_mc_core.web_api.handler

import kotlinx.serialization.Serializable
import online.ruin_of_future.informative_mc_core.data.ModData
import online.ruin_of_future.informative_mc_core.auth.TokenManager
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream

val UserTestApiId = ApiID("imc-manage", "test-user")

// TODO: Lift `requestStatus` and `requestInfo` out.

@Serializable
data class UserTestResponse(
    val requestStatus: String,
    val requestInfo: String,
    val userName: String,
) {
    companion object {
        fun success(userName: String): UserTestResponse {
            return UserTestResponse(
                requestStatus = "success",
                requestInfo = "",
                userName = userName,
            )
        }

        fun unknownUserName(userName: String): UserTestResponse {
            return UserTestResponse(
                requestStatus = "error",
                requestInfo = "unknown username",
                userName = userName,
            )
        }

        fun invalidToken(userName: String): UserTestResponse {
            return UserTestResponse(
                requestStatus = "error",
                requestInfo = "invalid token",
                userName = userName,
            )
        }
    }
}

class UserTestHandler(
    private val tokenManager: TokenManager,
    private val modData: ModData,
) : ParamPostHandler() {
    override val id: ApiID = UserTestApiId

    override fun handleRequest(formParams: Map<String, List<String>>, outputStream: OutputStream) {
        val req = parseUserRequest(formParams)
        if (!modData.hasUserName(req.userName)) {
            UserTestResponse.unknownUserName(req.userName).writeToStream(outputStream)
        } else if (!tokenManager.verify(req.token)) {
            UserTestResponse.invalidToken(req.userName).writeToStream(outputStream)
        } else {
            UserTestResponse.success(req.userName).writeToStream(outputStream)
        }
    }
}