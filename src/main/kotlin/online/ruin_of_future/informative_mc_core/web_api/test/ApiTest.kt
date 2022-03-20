package online.ruin_of_future.informative_mc_core.web_api.test

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import online.ruin_of_future.informative_mc_core.web_api.response.ApiResponse
import java.util.*
import java.util.concurrent.TimeUnit

interface ApiTest<ResponseT : ApiResponse<*>> {
    val client: OkHttpClient
        get() = OkHttpClient
            .Builder()
            .ignoreAllSSLErrors()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .writeTimeout(2, TimeUnit.SECONDS)
            .callTimeout(2, TimeUnit.SECONDS)
            .build()

    val serverAddress: String
        get() = "https://localhost:3030/"
    val apiId: ApiId
    val apiAddress: String
        get() = serverAddress.smartAppendApiAddress(apiId.toURIString())

    fun checkResponse(response: ResponseT) {
        assert(response.requestStatus == "success")
        assert(response.responseDetail != null)
    }

    suspend fun runWithCallback(
        onSuccess: (response: ResponseT) -> Unit = {},
        onFailure: (cause: Throwable) -> Unit = {},
    )
}

/**
 * [ApiTest] is an interface and has a default implementation.
 * In most cases default implementation is enough.
 * Non-default implementation is used when some
 * special operations are needed.
 * */
class PostApiTestImpl<ResponseT : ApiResponse<*>>(
    override val apiId: ApiId,
    private val username: String,
    private val tokenUUID: UUID,
    private val responseSerializer: KSerializer<ResponseT>
) : ApiTest<ResponseT> {

    override suspend fun runWithCallback(
        onSuccess: (response: ResponseT) -> Unit,
        onFailure: (cause: Throwable) -> Unit
    ) {
        val formBody = FormBody
            .Builder()
            .add("username", username)
            .add("token", tokenUUID.toString())
            .build()
        val request = Request
            .Builder()
            .url(apiAddress)
            .post(formBody)
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.code != 200) {
                assert(false)
            } else {
                val body = Json.decodeFromString(responseSerializer, response.body!!.string())
                checkResponse(body)
                onSuccess(body)
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}

/**
 * Execution order of tests in the same batch is not guaranteed.
 * */
interface ApiTestBatch {
    val passedTest: MutableMap<ApiId, ApiResponse<*>>
    val failedTest: MutableMap<ApiId, Throwable>
    val name: String
    suspend fun runWithCallback(
        onSuccess: (passedTest: Map<ApiId, ApiResponse<*>>) -> Unit,
        onFailure: (failedTest: Map<ApiId, Throwable>) -> Unit,
    )
}

/**
 * Default parallel implementation of [ApiTestBatch]
 * */
class ApiTestBatchAsync(
    override val name: String,
    private val tests: List<ApiTest<*>>
) : ApiTestBatch {
    override val passedTest = mutableMapOf<ApiId, ApiResponse<*>>()
    override val failedTest = mutableMapOf<ApiId, Throwable>()

    override suspend fun runWithCallback(
        onSuccess: (passedTest: Map<ApiId, ApiResponse<*>>) -> Unit,
        onFailure: (failedTest: Map<ApiId, Throwable>) -> Unit
    ) = coroutineScope {
        tests.map {
            async {
                it.runWithCallback(
                    onSuccess = { response ->
                        passedTest[it.apiId] = response
                    },
                    onFailure = { cause ->
                        failedTest[it.apiId] = cause
                    }
                )
            }
        }.forEach {
            it.await()
        }
        onSuccess(passedTest.toMap())
        onFailure(failedTest.toMap())
    }
}