package online.ruin_of_future.informative_mc_core.web_api.handler

import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream

abstract class ParaFreeApiHandler {
    abstract fun handleRequest(outputStream: OutputStream)
    abstract val id: ApiID

    // If you need to init something after mod loaded, override setup().
    fun setup() {}
}