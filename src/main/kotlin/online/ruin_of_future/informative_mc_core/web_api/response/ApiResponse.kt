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

@Serializable
sealed class ApiResponse<T : Any> {
    abstract val requestStatus: String
    abstract val requestInfo: String
    abstract val responseDetail: T?
}

/**
 * Common responses for APIs that need auth.
 * The reason of using generic parameter R as return type
 * instead of `ApiResponse<T>` is that polymorphic deserializer
 * can only recognize concrete types.
 * */
sealed class ApiAuthCommonResponses<T : Any, R>(
    private val responseBuilder: (String, String, T?) -> R
) {
    fun success(body: T): R {
        return responseBuilder("success", "", body)
    }

    fun invalidTokenError(uuid: UUID): R {
        return responseBuilder("error", "invalid token: $uuid", null)
    }

    fun usernameError(userName: String): R {
        return responseBuilder("error", "wrong or unknown username: $userName", null)
    }

    fun error(info: String): R {
        return responseBuilder("error", info, null)
    }

    fun unknownError(): R {
        return responseBuilder("error", "unknown error", null)
    }
}