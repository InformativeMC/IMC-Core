package online.ruin_of_future.informative_mc_api_core

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class ModConfig(val port: String) {
    companion object {
        val DEFAULT = ModConfig("3030")
    }
}