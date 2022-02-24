package online.ruin_of_future.informative_mc_api_core.web_api

/**
 * A sample API which indicates server health.
 * */
class HeatBeat : ApiBase<Nothing, Nothing>() {
    override val id: ApiID
        get() = ApiID("SystemInfo", "HeatBeat")

    override fun invoke(args: Map<Nothing, Nothing>?): Boolean {
        // Returning true means everything is OK.
        return true
    }
}