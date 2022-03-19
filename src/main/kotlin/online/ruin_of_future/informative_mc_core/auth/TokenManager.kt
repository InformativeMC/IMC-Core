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
package online.ruin_of_future.informative_mc_core.auth

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

// TODO: persistent storage
class TokenManager {
    class AddExistingTokenException(msg: String) : Exception(msg)

    private val tokenBin = ConcurrentHashMap<UUID, Token>()
    private val timer = Timer("TokenSystem", true)

    fun addTimedToken(expiredAfterMillis: Long = TimeUnit.SECONDS.toMillis(60 * 5)): TimedToken {
        var token = TimedToken(UUID.randomUUID(), Date().time, expiredAfterMillis)
        while (tokenBin.containsKey(token.uuid)) {
            token = TimedToken(UUID.randomUUID(), Date().time, expiredAfterMillis)
        }
        tokenBin[token.uuid] = token
        return token
    }

    fun addTimedToken(
        uuid: UUID,
        expiredAfterMillis: Long = TimeUnit.SECONDS.toMillis(60 * 5)
    ): TimedToken {
        if (tokenBin.containsKey(uuid)) {
            throw AddExistingTokenException("$uuid is already in token bin!")
        } else {
            return TimedToken(uuid, Date().time, expiredAfterMillis)
        }
    }

    fun addOnceToken(): OnceToken {
        var token = OnceToken(UUID.randomUUID())
        while (tokenBin.containsKey(token.uuid)) {
            token = OnceToken(UUID.randomUUID())
        }
        tokenBin[token.uuid] = token
        return token
    }

    fun addOnceToken(uuid: UUID): OnceToken {
        if (tokenBin.containsKey(uuid)) {
            throw AddExistingTokenException("$uuid is already in token bin!")
        } else {
            return OnceToken(uuid)
        }
    }

    fun addTimedOnceToken(expiredAfterMillis: Long = TimeUnit.SECONDS.toMillis(60 * 5)): TimedOnceToken {
        var token = TimedOnceToken(UUID.randomUUID(), Date().time, expiredAfterMillis)
        while (tokenBin.containsKey(token.uuid)) {
            token = TimedOnceToken(UUID.randomUUID(), Date().time, expiredAfterMillis)
        }
        tokenBin[token.uuid] = token
        return token
    }

    fun addTimedOnceToken(
        uuid: UUID,
        expiredAfterMillis: Long = TimeUnit.SECONDS.toMillis(60 * 5)
    ): TimedOnceToken {
        if (tokenBin.containsKey(uuid)) {
            throw AddExistingTokenException("$uuid is already in token bin!")
        } else {
            return TimedOnceToken(uuid, Date().time, expiredAfterMillis)
        }
    }

    fun addForeverToken(): ForeverToken {
        var token = ForeverToken(UUID.randomUUID())
        while (tokenBin.containsKey(token.uuid)) {
            token = ForeverToken(UUID.randomUUID())
        }
        tokenBin[token.uuid] = token
        return token
    }

    fun addForeverToken(uuid: UUID): ForeverToken {
        if (tokenBin.containsKey(uuid)) {
            throw AddExistingTokenException("$uuid is already in token bin!")
        } else {
            return ForeverToken(uuid)
        }
    }

    fun hasToken(uuid: UUID): Boolean {
        return tokenBin.containsKey(uuid)
    }

    fun verifyToken(uuid: UUID): Boolean {
        return tokenBin[uuid]?.isValid() == true
    }

    fun getToken(uuid: UUID): Token? {
        return tokenBin[uuid]
    }

    private fun clearOutdatedToken() {
        tokenBin.forEach { (k, v) ->
            if (!v.isValid()) {
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