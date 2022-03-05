package online.ruin_of_future.informative_mc_core.web_api

import java.io.OutputStream

abstract class ParaFreeApiHandler {
    abstract fun handle(outputStream: OutputStream)
    abstract val id: ApiID
}