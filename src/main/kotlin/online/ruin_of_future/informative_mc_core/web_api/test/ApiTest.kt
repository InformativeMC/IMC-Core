package online.ruin_of_future.informative_mc_core.web_api.test

import okhttp3.OkHttpClient
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import online.ruin_of_future.informative_mc_core.web_api.response.ApiResponse
import java.util.concurrent.TimeUnit

/**
 * Try to tests are independent and stateless.
 * */
abstract class ApiTest<ResponseT : ApiResponse<*>> {
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
        onSuccess: (response: ResponseT) -> Unit = {},
        onFailure: (cause: Throwable) -> Unit = {},
    )
}

/**
 * Tests in the same `ApiTestBatch` are executed in order.
 * */
sealed interface ApiTestBatch {
    val passedTest: MutableMap<ApiId, ApiResponse<*>>
    val failedTest: MutableMap<ApiId, Throwable>
    val name: String
    suspend fun runWithCallback(
        onSuccess: (passedTest: Map<ApiId, ApiResponse<*>>) -> Unit,
        onFailure: (failedTest: Map<ApiId, Throwable>) -> Unit,
    )
}