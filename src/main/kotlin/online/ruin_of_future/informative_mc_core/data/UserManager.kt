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

import online.ruin_of_future.informative_mc_core.core.dbFilePath
import online.ruin_of_future.informative_mc_core.database.SqliteDBTable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.sql.ResultSet
import java.util.*

class UserManager(
    dbTableName: String
) : SqliteDBTable<ImcUser>(
    name = dbTableName,
    sqlLiteDBPath = dbFilePath,
    createIfAbsent = true,
) {
    companion object {
        @JvmStatic
        private val LOGGER: Logger = LogManager.getLogger("IMC User")
    }

    fun hasUser(username: String): Boolean {
        return select("username = '$username'").isNotEmpty()
    }

    fun getUser(username: String): ImcUser? {
        return select("username = '$username'").getOrNull(0)
    }

    fun addUser(userName: String): ImcUser {
        val newUser = ImcUser.create(userName, UUID.randomUUID())
        insert(newUser)
        LOGGER.info("A new user added: ${newUser.username}")
        return newUser
    }

    fun verifyUserToken(username: String, userToken: UUID): Boolean {
        return select("username = '$username' AND userToken='$userToken'").isNotEmpty()
    }

    override fun tableSchema(): String {
        val columns = listOf(
            "id int",
            "username varchar(255)",
            "userToken varchar(255)",
        )
        val sb = StringBuilder()
        sb.append('(')
        columns.forEachIndexed { idx, entry ->
            sb.append(entry)
            if (idx < columns.size - 1) {
                sb.append(',')
            }
        }
        sb.append(')')
        return sb.toString()
    }

    override fun resultSetParser(rs: ResultSet): List<ImcUser> {
        val result = mutableListOf<ImcUser>()
        while (rs.next()) {
            val username = rs.getString("username")
            val token = UUID.fromString(rs.getString("userToken"))
            result.add(ImcUser.create(username, token))
        }
        return result
    }
}