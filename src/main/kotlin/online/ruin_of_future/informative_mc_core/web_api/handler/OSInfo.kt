package online.ruin_of_future.informative_mc_core.web_api.handler

import kotlinx.serialization.Serializable
import online.ruin_of_future.informative_mc_core.humanReadableSize
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream

val OSInfoApiId = ApiID("system-info", "os-info")

@Suppress("UnUsed")
@Serializable
class OSInfo private constructor(
    // OS Info
    val osName: String,
    val maxMemory: String,
    val allocatedMemory: String,
    val freeMemory: String,
    override val id: ApiID = OSInfoApiId,
) : ParamFreeHandler() {

    constructor() : this("???", "???", "???", "???")

    override fun handleRequest(outputStream: OutputStream) {
        OSInfo(
            System.getProperty("os.name") ?: "unknown",
            Runtime.getRuntime().maxMemory().humanReadableSize(),
            Runtime.getRuntime().totalMemory().humanReadableSize(),
            Runtime.getRuntime().freeMemory().humanReadableSize(),
        ).writeToStream(outputStream)
    }
}