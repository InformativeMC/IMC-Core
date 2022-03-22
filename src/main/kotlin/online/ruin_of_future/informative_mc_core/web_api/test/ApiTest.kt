/*
 * Copyright (c) 2022 InformativeMC
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/lgpl-3.0.txt>.
 */
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * [ApiTest] is an interface of Api uni test.
 * And it has some default implementations.
 * In most cases default implementations are enough.
 * Non-default implementations are preferred when some
 * special operations are needed. Make sure to enable `-ea`
 * option for JVM. Otherwise, all assertions have no effect.
 * */
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
        assert(response.requestStatus == "success") { "request not success" }
        assert(response.responseDetail != null) { "no valid response detail" }
    }

    suspend fun runWithCallback(
        onSuccess: (response: ResponseT) -> Unit = {},
        onFailure: (cause: Throwable) -> Unit = {},
    )
}

/**
 * Default implementation of [ApiTest] for **post** methods.
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
                assert(false) { "connection issue: ${response.code}" }
            } else {
                val body = Json.decodeFromString(responseSerializer, response.body!!.string())
                checkResponse(body)
                onSuccess(body)
            }
        } catch (e: Throwable) {
            onFailure(e)
        }
    }
}

/**
 * Default implementation of [ApiTest] for **GET** methods.
 * */
class GetApiTestImpl<ResponseT : ApiResponse<*>>(
    override val apiId: ApiId,
    private val responseSerializer: KSerializer<ResponseT>
) : ApiTest<ResponseT> {
    override suspend fun runWithCallback(
        onSuccess: (response: ResponseT) -> Unit,
        onFailure: (cause: Throwable) -> Unit
    ) {
        val request = Request
            .Builder()
            .url(apiAddress)
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.code != 200) {
                assert(false) { "connection issue: ${response.code}" }
            } else {
                val body = Json.decodeFromString(responseSerializer, response.body!!.string())
                checkResponse(body)
                onSuccess(body)
            }
        } catch (e: Throwable) {
            onFailure(e)
        }
    }
}

/**
 * Execution order of tests in the same batch is not guaranteed.
 * The implementation determines it.
 * */
interface ApiTestBatch {
    val passedTest: Map<ApiId, ApiResponse<*>>
    val failedTest: Map<ApiId, Throwable>
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
    override val passedTest = ConcurrentHashMap<ApiId, ApiResponse<*>>()
    override val failedTest = ConcurrentHashMap<ApiId, Throwable>()

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