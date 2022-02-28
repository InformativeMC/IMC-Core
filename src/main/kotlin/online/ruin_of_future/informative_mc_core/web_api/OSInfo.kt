package online.ruin_of_future.informative_mc_core.web_api

import online.ruin_of_future.informative_mc_core.humanReadableSize
import kotlinx.serialization.Serializable

val OSInfoApiId = ApiID("system-info", "os-info")

@Serializable
data class OSInfo(
    // OS Info
    val osName: String,
    val maxMemory: String,
    val allocatedMemory: String,
    val freeMemory: String,
    val id: ApiID = OSInfoApiId,
)  {
    companion object {
        fun handle(): OSInfo {
            return OSInfo(
                System.getProperty("os.name") ?: "unknown",
                Runtime.getRuntime().maxMemory().humanReadableSize(),
                Runtime.getRuntime().totalMemory().humanReadableSize(),
                Runtime.getRuntime().freeMemory().humanReadableSize(),
            )
        }
    }
}