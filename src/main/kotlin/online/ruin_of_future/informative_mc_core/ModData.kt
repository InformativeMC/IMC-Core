package online.ruin_of_future.informative_mc_core

import kotlinx.serialization.Serializable

@Serializable
class ModData(
) {
    companion object {
        lateinit var CURRENT: ModData
        val DEFAULT = ModData()
    }
}