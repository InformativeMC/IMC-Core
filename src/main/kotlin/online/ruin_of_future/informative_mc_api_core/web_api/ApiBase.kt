package online.ruin_of_future.informative_mc_api_core.web_api

data class ApiID(val namespace: String, val path: String)

@Suppress("UnUsed")
abstract class ApiBase<K, V, R> {
    abstract val id: ApiID

    protected open fun onInit() {}

    abstract operator fun invoke(args: Map<K, V>? = null): R

    init {
        this.onInit()
    }
}