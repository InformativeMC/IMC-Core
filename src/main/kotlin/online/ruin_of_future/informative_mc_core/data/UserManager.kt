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

import online.ruin_of_future.informative_mc_core.auth.Token
import online.ruin_of_future.informative_mc_core.auth.TokenManager
import org.apache.logging.log4j.LogManager
import java.util.*

class UserManager {
    private val LOGGER = LogManager.getLogger("IMC User")

    // TODO: persistent storage
    private val users = mutableMapOf<String, ImcUser>()
    private val userTokenManager = TokenManager()

    fun hasUserName(userName: String): Boolean {
        return users.containsKey(userName)
    }

    fun hasUser(userName: String, imcUser: ImcUser): Boolean {
        return users[userName]?.username == imcUser.username &&
                users[userName]?.userToken == imcUser.userToken
    }

    fun hasUser(userName: String, userToken: Token): Boolean {
        return users.containsKey(userName) && users[userName]?.userToken == userToken
    }

    fun addUser(userName: String): ImcUser {
        val newUser = ImcUser(userName, userTokenManager.addForeverToken())
        LOGGER.info("A new user added: ${newUser.username}")
        users[newUser.username] = newUser
        return newUser
    }

    fun removeUser(userName: String): Boolean {
        return if (users.containsKey(userName)) {
            users.remove(userName, users[userName])
        } else {
            false
        }
    }

    fun verifyUserToken(userName: String, token: UUID): Boolean {
        return userTokenManager.verifyToken(token) && users[userName]?.userToken?.uuid == token
    }
}