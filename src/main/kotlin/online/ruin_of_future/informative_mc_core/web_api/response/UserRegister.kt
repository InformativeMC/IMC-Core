package online.ruin_of_future.informative_mc_core.web_api.response

import kotlinx.serialization.Serializable
import online.ruin_of_future.informative_mc_core.util.UUIDSerializer
import java.util.*

@Serializable
class UserRegisterResponseBody(
    val userName: String,
    @Serializable(with = UUIDSerializer::class)
    val key: UUID,
)

@Serializable
class UserRegisterResponse(
    override val requestStatus: String,
    override val requestInfo: String,
    override val responseBody: UserRegisterResponseBody?
) : ApiResponse<UserRegisterResponseBody?>() {
    companion object {
        fun success(userName: String, key: UUID): UserRegisterResponse {
            return UserRegisterResponse(
                requestStatus = "success",
                requestInfo = "",
                responseBody = UserRegisterResponseBody(userName, key)
            )
        }

        fun usedUsernameError(userName: String, key: UUID): UserRegisterResponse {
            return UserRegisterResponse(
                requestStatus = "error",
                requestInfo = "already occupied username",
                responseBody = UserRegisterResponseBody(userName, key)
            )
        }

        fun invalidTokenError(userName: String, key: UUID): UserRegisterResponse {
            return UserRegisterResponse(
                requestStatus = "error",
                requestInfo = "not a valid token",
                responseBody = UserRegisterResponseBody(userName, key)
            )
        }

        fun unknownError(): UserRegisterResponse {
            return UserRegisterResponse(
                requestStatus = "error",
                requestInfo = "unknown error",
                responseBody = null
            )
        }
    }
}