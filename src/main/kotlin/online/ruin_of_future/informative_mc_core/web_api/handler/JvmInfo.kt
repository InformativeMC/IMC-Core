package online.ruin_of_future.informative_mc_core.web_api.handler

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream

val JvmInfoApiID = ApiID("system-info", "jvm-info")

@Suppress("UnUsed")
@Serializable
class JvmInfo private constructor(
    // Jvm Info
    val jvmName: String,
    val jvmVendor: String,
    val jvmVersion: String,
    val jvmInfo: String,

    // Java & Kotlin version
    val javaVersion: String,
    val kotlinVersion: String,
    override val id: ApiID = JvmInfoApiID,
) : ParamFreeHandler() {
    constructor() : this("???", "???", "???", "???", "???", "???")

    override fun handleRequest(outputStream: OutputStream) {
        JvmInfo(
            jvmName = System.getProperty("java.vm.name") ?: "unknown",
            jvmVendor = System.getProperty("java.vm.vendor") ?: "unknown",
            jvmVersion = System.getProperty("java.vm.version") ?: "unknown",
            jvmInfo = System.getProperty("Java.vm.info") ?: "unknown",
            javaVersion = System.getProperty("java.version") ?: "unknown",
            kotlinVersion = KotlinVersion.CURRENT.toString(),
        ).writeToStream(outputStream)
    }
}
