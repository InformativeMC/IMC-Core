package online.ruin_of_future.informative_mc_core.web_api.handler

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
abstract class ApiHandler {
    abstract val id: ApiID

    // If you need to init something after mod loaded, override setup().
    fun setup() {}

    inline fun <reified T> T.writeToStream(outputStream: OutputStream) {
        Json.encodeToStream(this, outputStream)
    }
}

abstract class ParamFreeHandler : ApiHandler() {
    abstract fun handleRequest(outputStream: OutputStream)
}

abstract class ParamGetHandler : ApiHandler() {
    abstract fun handleRequest(
        queryParamMap: Map<String, List<String>>,
        outputStream: OutputStream
    )
}