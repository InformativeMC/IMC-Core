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

import kotlinx.serialization.Serializable
import org.apache.logging.log4j.LogManager
import java.util.*

@Serializable
class ModData(
    // TODO: Concurrent
    private val users: HashMap<String, ImcUser>
) {
    private val LOGGER = LogManager.getLogger("IMC Data")

    fun hasUserName(userName: String): Boolean {
        return users.containsKey(userName)
    }

    fun hasUser(userName: String, imcUser: ImcUser): Boolean {
        return users[userName]?.userName == imcUser.userName &&
                users[userName]?.userTokenId == imcUser.userTokenId
    }

    fun addUser(newUser: ImcUser) {
        LOGGER.info("A new user added: ${newUser.userName}")
        users[newUser.userName] = newUser
    }

    fun removeUser(userName: String): Boolean {
        return if (users.containsKey(userName)) {
            users.remove(userName, users[userName])
        } else {
            false
        }
    }


    companion object {
        val DEFAULT = ModData(
            users = hashMapOf()
        )
    }
}