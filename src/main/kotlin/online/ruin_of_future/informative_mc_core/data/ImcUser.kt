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
package online.ruin_of_future.informative_mc_core.data

import online.ruin_of_future.informative_mc_core.database.SqlTableRow
import java.util.*

class ImcUser private constructor(
    // Primary key
    private val id: Int,
    val username: String,
    val userToken: UUID,
) : SqlTableRow {
    override fun toSqlString(): String {
        return "($id, '$username', '$userToken')"
    }

    companion object {
        fun create(username: String, userToken: UUID): ImcUser {
            return ImcUser(-1, username, userToken)
        }
    }
}
