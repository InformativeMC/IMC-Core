package online.ruin_of_future.informative_mc_core.web_api

import kotlinx.serialization.Serializable

@Serializable
data class ApiID(val namespace: String, val path: String) {
    fun toURIString(): String {
        return "$namespace/$path"
    }
}