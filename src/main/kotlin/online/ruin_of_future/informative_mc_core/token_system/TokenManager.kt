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
package online.ruin_of_future.informative_mc_core.token_system

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

// TODO: persistent storage

class TokenManager {
    private val tokenBin = ConcurrentHashMap<UUID, Token>()
    private val timer = Timer("TokenSystem", true)

    fun addTimedToken(expiredAfterMillis: Long = TimeUnit.SECONDS.toMillis(60 * 5)): Token {
        var token = TimedToken(UUID.randomUUID(), Date().time, expiredAfterMillis)
        while (tokenBin.containsKey(token.uuid)) {
            token = TimedToken(UUID.randomUUID(), Date().time, expiredAfterMillis)
        }
        tokenBin[token.uuid] = token
        return token
    }

    fun addOnceToken(): Token {
        var token = OnceToken(UUID.randomUUID(), Date().time)
        while (tokenBin.containsKey(token.uuid)) {
            token = OnceToken(UUID.randomUUID(), Date().time)
        }
        tokenBin[token.uuid] = token
        return token
    }

    fun addForeverToken(): Token {
        var token = ForeverToken(UUID.randomUUID(), Date().time)
        while (tokenBin.containsKey(token.uuid)) {
            token = ForeverToken(UUID.randomUUID(), Date().time)
        }
        tokenBin[token.uuid] = token
        return token
    }

    private fun clearOutdatedToken() {
        tokenBin.forEach { (k, v) ->
            if (v.isValid()) {
                tokenBin.remove(k)
            }
        }
    }

    init {
        timer.schedule(
            delay = TimeUnit.SECONDS.toMillis(30),
            period = TimeUnit.SECONDS.toMillis(30)
        ) {
            clearOutdatedToken()
        }
    }
}