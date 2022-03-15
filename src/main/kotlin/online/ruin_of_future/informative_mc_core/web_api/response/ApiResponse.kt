package online.ruin_of_future.informative_mc_core.web_api.response

import kotlinx.serialization.Serializable

@Serializable
sealed class ApiResponse<T> {
    abstract val requestStatus: String
    abstract val requestInfo: String
    abstract val responseBody: T
}