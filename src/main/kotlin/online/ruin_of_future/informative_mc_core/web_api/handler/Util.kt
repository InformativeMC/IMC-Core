package online.ruin_of_future.informative_mc_core.web_api.handler

import java.util.*

data class UserRequest(
    val userName: String,
    val token: UUID,
)

fun parseUserRequest(
    formParamMap: Map<String, List<String>>
): UserRequest {
    val u = formParamMap["token"]?.get(0)
        ?: throw MissingParameterException("Need token for register")
    val uuid = try {
        UUID.fromString(u)
    } catch (e: IllegalArgumentException) {
        UUID.randomUUID() // useless
    }
    return UserRequest(
        userName = formParamMap["userName"]?.get(0)
            ?: throw MissingParameterException("Need user name for register"),
        token = uuid,
    )
}