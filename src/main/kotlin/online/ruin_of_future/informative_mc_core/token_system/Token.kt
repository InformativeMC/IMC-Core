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

sealed class Token(
    val uuid: UUID,
    val created: Long,
) {
    open fun isValid(): Boolean {
        return false
    }

    abstract override fun toString(): String
}

class OnceToken(
    uuid: UUID,
    created: Long,
) : Token(uuid, created) {
    var used = false

    override fun isValid(): Boolean {
        return !used
    }

    override fun toString(): String {
        // TODO: I18n
        return "$uuid (can only be used once)"
    }
}

class TimedToken(
    uuid: UUID,
    created: Long,
    val expiredAfterMillis: Long
) : Token(uuid, created) {
    override fun isValid(): Boolean {
        return created + expiredAfterMillis > Date().time
    }

    override fun toString(): String {
        // TODO: I18n
        val expiredTime = Date(created + expiredAfterMillis).toString()
        return "$uuid (expired at $expiredTime)"
    }
}

class ForeverToken(
    uuid: UUID,
    created: Long,
) : Token(uuid, created) {
    override fun isValid(): Boolean {
        return true
    }

    override fun toString(): String {
        return "$uuid (forever valid)"
    }
}