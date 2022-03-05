package online.ruin_of_future.informative_mc_core.web_api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.OutputStream

val JvmInfoApiID = ApiID("system-info", "jvm-info")

@Suppress("UnUsed")
@OptIn(ExperimentalSerializationApi::class)
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
    override val id: ApiID = JvmInfoApiID,
) : ParaFreeApiHandler() {
    override fun handle(outputStream: OutputStream) {
        val info = JvmInfo(
            System.getProperty("java.vm.name") ?: "unknown",
            System.getProperty("java.vm.vendor") ?: "unknown",
            System.getProperty("java.vm.version") ?: "unknown",
            System.getProperty("Java.vm.info") ?: "unknown",
            System.getProperty("java.version") ?: "unknown",
            KotlinVersion.CURRENT.toString(),
        )
        Json.encodeToStream(info, outputStream)
    }
}
