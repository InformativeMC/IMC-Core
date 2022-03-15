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
import online.ruin_of_future.informative_mc_core.util.UUIDSerializer
import java.util.*

@Serializable
class UserRegisterResponseBody(
    val userName: String,
    @Serializable(with = UUIDSerializer::class)
    val key: UUID,
)

@Serializable
class UserRegisterResponse(
    override val requestStatus: String,
    override val requestInfo: String,
    override val responseBody: UserRegisterResponseBody?
) : ApiResponse<UserRegisterResponseBody?>() {
    companion object {
        fun success(userName: String, key: UUID): UserRegisterResponse {
            return UserRegisterResponse(
                requestStatus = "success",
                requestInfo = "",
                responseBody = UserRegisterResponseBody(userName, key)
            )
        }

        fun usedUsernameError(userName: String, key: UUID): UserRegisterResponse {
            return UserRegisterResponse(
                requestStatus = "error",
                requestInfo = "already occupied username",
                responseBody = UserRegisterResponseBody(userName, key)
            )
        }

        fun invalidTokenError(userName: String, key: UUID): UserRegisterResponse {
            return UserRegisterResponse(
                requestStatus = "error",
                requestInfo = "not a valid token",
                responseBody = UserRegisterResponseBody(userName, key)
            )
        }

        fun unknownError(): UserRegisterResponse {
            return UserRegisterResponse(
                requestStatus = "error",
                requestInfo = "unknown error",
                responseBody = null
            )
        }
    }
}