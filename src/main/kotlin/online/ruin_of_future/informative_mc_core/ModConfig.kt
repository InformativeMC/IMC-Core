package online.ruin_of_future.informative_mc_core

import kotlinx.serialization.*

@Serializable
data class ModConfig(val port: Int) {
    companion object {
        val DEFAULT = ModConfig(3030)
    }
}