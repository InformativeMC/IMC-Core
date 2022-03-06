package online.ruin_of_future.informative_mc_core

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class ModConfig(
    val port: Int,
    val keyStorePath: String,
) {
    companion object {
        val DEFAULT = ModConfig(
            port = 3030,
            keyStorePath = "$modConfigDirPath${File.separatorChar}IMC-Core.jks",
        )

        lateinit var CURRENT: ModConfig
    }
}