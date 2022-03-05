package online.ruin_of_future.informative_mc_core.web_api

import kotlinx.serialization.ExperimentalSerializationApi
import online.ruin_of_future.informative_mc_core.humanReadableSize
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.OutputStream

val OSInfoApiId = ApiID("system-info", "os-info")

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class OSInfo(
    // OS Info
    val osName: String,
    val maxMemory: String,
    val allocatedMemory: String,
    val freeMemory: String,
    override val id: ApiID = OSInfoApiId,
) : ParaFreeApiHandler() {

    override fun handle(outputStream: OutputStream) {
        val info = OSInfo(
            System.getProperty("os.name") ?: "unknown",
            Runtime.getRuntime().maxMemory().humanReadableSize(),
            Runtime.getRuntime().totalMemory().humanReadableSize(),
            Runtime.getRuntime().freeMemory().humanReadableSize(),
        )
        Json.encodeToStream(info, outputStream)
    }
}