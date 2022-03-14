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

import kotlinx.serialization.Serializable
import online.ruin_of_future.informative_mc_core.util.UUIDSerializer
import java.util.*

@Serializable
sealed class Token(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
) {
    abstract fun isValid(): Boolean

    abstract override fun toString(): String
}

interface Usable {
    fun isUsed(): Boolean
    fun setUsed()
}

interface WillExpire {
    fun isExpired(): Boolean
    fun expiredAt(): Date
}

class UsableImpl : Usable {
    private var used = false

    override fun isUsed(): Boolean {
        return used
    }

    override fun setUsed() {
        used = true
    }
}

class WillExpireImpl(
    public val createdAtMillis: Long,
    public val expireAfterMillis: Long,
) : WillExpire {
    override fun isExpired(): Boolean {
        return createdAtMillis + expireAfterMillis < Date().time
    }

    override fun expiredAt(): Date {
        return Date(createdAtMillis + expireAfterMillis)
    }
}

class OnceToken(
    uuid: UUID,
) : Token(uuid), Usable by UsableImpl() {

    override fun isValid(): Boolean {
        return isUsed()
    }

    override fun toString(): String {
        // TODO: I18n
        return "$uuid (can only be used once)"
    }
}

class TimedToken(
    uuid: UUID,
    createdAtMillis: Long,
    expiredAfterMillis: Long
) : Token(uuid), WillExpire by WillExpireImpl(createdAtMillis, expiredAfterMillis) {
    override fun isValid(): Boolean {
        return !isExpired()
    }

    override fun toString(): String {
        // TODO: I18n
        return "$uuid (valid before ${expiredAt()})"
    }
}

class TimedOnceToken(
    uuid: UUID,
    createdAtMillis: Long,
    expiredAfterMillis: Long
) :
    Token(uuid),
    WillExpire by WillExpireImpl(createdAtMillis, expiredAfterMillis),
    Usable by UsableImpl() {

    override fun toString(): String {
        // TODO: I18n
        return "$uuid (can be used once before ${expiredAt()})"
    }

    override fun isValid(): Boolean {
        return !isUsed() && !isExpired()
    }
}

class ForeverToken(
    uuid: UUID,
) : Token(uuid) {
    override fun isValid(): Boolean {
        return true
    }

    override fun toString(): String {
        return "$uuid (forever valid)"
    }
}