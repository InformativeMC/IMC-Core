package online.ruin_of_future.informative_mc_core.web_api.test

import okhttp3.OkHttpClient
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import java.util.concurrent.TimeUnit


/**
 * Try to tests are independent and stateless.
 * */
abstract class ApiTest {
    protected open val client = OkHttpClient
        .Builder()
        .ignoreAllSSLErrors()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.SECONDS)
        .writeTimeout(2, TimeUnit.SECONDS)
        .callTimeout(2, TimeUnit.SECONDS)
        .build()

    open val serverAddress = "https://localhost:3030/"
    abstract val apiId: ApiId
    val apiAddress: String
        get() = serverAddress.smartAppendApiAddress(apiId.toURIString())

    abstract suspend fun runWithCallback(
        onSuccess: () -> Unit = {},
        onFailure: (cause: Throwable) -> Unit = {},
    )
}