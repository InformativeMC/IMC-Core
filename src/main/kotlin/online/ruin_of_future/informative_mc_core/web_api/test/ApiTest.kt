package online.ruin_of_future.informative_mc_core.web_api.test

import okhttp3.OkHttpClient
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import org.apache.logging.log4j.LogManager
import java.util.concurrent.TimeUnit


/**
 * Try to tests are independent and stateless.
 * */
abstract class ApiTest {
    protected val LOGGER = LogManager.getLogger("IMC API Test")
    protected open val client = OkHttpClient
        .Builder()
        .ignoreAllSSLErrors()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.SECONDS)
        .writeTimeout(2, TimeUnit.SECONDS)
        .callTimeout(2, TimeUnit.SECONDS)
        .build()

    open val serverAddress = "https://localhost:3030/"
    abstract val apiId: ApiID
    val apiAddress: String
        get() = serverAddress.smartAppendApiAddress(apiId.toURIString())

    abstract fun run(): Boolean
}