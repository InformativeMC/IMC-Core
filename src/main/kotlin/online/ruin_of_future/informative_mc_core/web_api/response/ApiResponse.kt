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
package online.ruin_of_future.informative_mc_core.web_api.response

import kotlinx.serialization.Serializable
import java.util.*


/**
 * Common responses for APIs that need auth.
 * The reason of using generic parameter R as return type
 * instead of `ApiResponse<T>` is that polymorphic deserializer
 * can only recognize concrete types.
 * */
sealed class ApiAuthCommonResponse<DetailT : Any, ResponseT>(
    private val responseBuilder: (String, String, DetailT?) -> ResponseT
) {
    fun success(detail: DetailT): ResponseT {
        return responseBuilder("success", "", detail)
    }

    fun invalidTokenError(uuid: UUID): ResponseT {
        return responseBuilder("error", "invalid token: $uuid", null)
    }

    fun usernameError(userName: String): ResponseT {
        return responseBuilder("error", "wrong or unknown username: $userName", null)
    }

    fun error(info: String): ResponseT {
        return responseBuilder("error", info, null)
    }

    fun unknownError(): ResponseT {
        return responseBuilder("error", "unknown error", null)
    }
}

@Serializable
sealed class ApiResponse<DetailT : Any> {
    abstract val requestStatus: String
    abstract val requestInfo: String
    abstract val responseDetail: DetailT?
}