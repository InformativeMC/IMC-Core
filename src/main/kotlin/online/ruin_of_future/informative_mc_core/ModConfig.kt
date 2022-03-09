package online.ruin_of_future.informative_mc_core

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class KeyStoreConfig(
    val keyStorePath: String,
)

@Serializable
data class CertConfig(
    val certPath: String,
    val keyPath: String,
)

@Serializable
class ModConfig private constructor(
    val port: Int,
    val password: String,
    val keyStoreConfig: KeyStoreConfig,
    val certConfig: CertConfig?,
) {
    companion object {
        val DEFAULT = ModConfig(
            port = 3030,
            password = generateRandomString(50),
            keyStoreConfig = KeyStoreConfig(
                keyStorePath = "$modConfigDirPath${File.separatorChar}IMC-Core.jks",
            ),
            certConfig = null,
        )

        lateinit var CURRENT: ModConfig
    }
}