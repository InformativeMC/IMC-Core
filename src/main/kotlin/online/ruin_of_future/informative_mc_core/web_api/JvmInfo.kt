package online.ruin_of_future.informative_mc_core.web_api

import kotlinx.serialization.Serializable

val JvmInfoApiID = ApiID("system-info", "jvm-info")

@Suppress("UnUsed")
@Serializable
data class JvmInfo(
    // Jvm Info
    val jvmName: String,
    val jvmVendor: String,
    val jvmVersion: String,
    val jvmInfo: String,

    // Java & Kotlin version
    val javaVersion: String,
    val kotlinVersion: String,
     val id: ApiID = JvmInfoApiID,
)  {
    companion object {
        fun handle(): JvmInfo {
            return JvmInfo(
                System.getProperty("java.vm.name") ?: "unknown",
                System.getProperty("java.vm.vendor") ?: "unknown",
                System.getProperty("java.vm.version") ?: "unknown",
                System.getProperty("Java.vm.info") ?: "unknown",
                System.getProperty("java.version") ?: "unknown",
                KotlinVersion.CURRENT.toString(),
            )
        }
    }
}
