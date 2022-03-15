package online.ruin_of_future.informative_mc_core.web_api.response

import kotlinx.serialization.Serializable

@Suppress("UnUsed")
@Serializable
class JvmInfoResponseBody(
    // Jvm Info
    val jvmName: String,
    val jvmVendor: String,
    val jvmVersion: String,
    val jvmInfo: String,

    // Java & Kotlin version
    val javaVersion: String,
    val kotlinVersion: String,
) {
    companion object {
        fun getCurrent(): JvmInfoResponseBody {
            return JvmInfoResponseBody(
                jvmName = System.getProperty("java.vm.name") ?: "unknown",
                jvmVendor = System.getProperty("java.vm.vendor") ?: "unknown",
                jvmVersion = System.getProperty("java.vm.version") ?: "unknown",
                jvmInfo = System.getProperty("Java.vm.info") ?: "unknown",
                javaVersion = System.getProperty("java.version") ?: "unknown",
                kotlinVersion = KotlinVersion.CURRENT.toString(),
            )
        }

        fun getEmpty(): JvmInfoResponseBody {
            return JvmInfoResponseBody(
                jvmName = "",
                jvmVendor = "",
                jvmVersion = "",
                jvmInfo = "",
                javaVersion = "",
                kotlinVersion = "",
            )
        }
    }
}

@Suppress("UnUsed")
@Serializable
class JvmInfoResponse(
    override val requestStatus: String,
    override val requestInfo: String,
    override val responseBody: JvmInfoResponseBody
) : ApiResponse<JvmInfoResponseBody>() {
    companion object {
        fun getCurrent(): JvmInfoResponse {
            return JvmInfoResponse(
                requestStatus = "success",
                requestInfo = "",
                responseBody = JvmInfoResponseBody.getCurrent()
            )
        }

        fun unknownUserError(userName: String): JvmInfoResponse {
            return JvmInfoResponse(
                requestStatus = "error",
                requestInfo = "unknown user: $userName",
                responseBody = JvmInfoResponseBody.getEmpty(),
            )
        }

        fun invalidTokenError(): JvmInfoResponse {
            return JvmInfoResponse(
                requestStatus = "error",
                requestInfo = "invalid token",
                responseBody = JvmInfoResponseBody.getEmpty(),
            )
        }
    }
}