package online.ruin_of_future.informative_mc_core.web_api.response

import kotlinx.serialization.Serializable

@Serializable
class UserTestResponseBody(
    val userName: String,
)

@Serializable
class UserTestResponse(
    override val requestStatus: String,
    override val requestInfo: String,
    override val responseBody: UserTestResponseBody?
) : ApiResponse<UserTestResponseBody?>() {
    companion object {
        fun success(userName: String): UserTestResponse {
            return UserTestResponse(
                requestStatus = "success",
                requestInfo = "",
                responseBody = UserTestResponseBody(userName),
            )
        }

        fun unknownUserNameError(userName: String): UserTestResponse {
            return UserTestResponse(
                requestStatus = "error",
                requestInfo = "unknown username",
                responseBody = UserTestResponseBody(userName),
            )
        }

        fun invalidTokenError(userName: String): UserTestResponse {
            return UserTestResponse(
                requestStatus = "error",
                requestInfo = "invalid token",
                responseBody = UserTestResponseBody(userName),
            )
        }
    }
}