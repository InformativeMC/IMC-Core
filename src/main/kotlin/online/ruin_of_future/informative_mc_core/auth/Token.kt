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
sealed class Token {
    @Serializable(with = UUIDSerializer::class)
    abstract val uuid: UUID

    abstract fun isValid(): Boolean

    abstract override fun toString(): String

    override fun equals(other: Any?): Boolean {
        return if (other is Token) {
            uuid == other.uuid
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}

interface Usable {
    fun isUsed(): Boolean
    fun setUsed()
}

interface WillExpire {
    fun isExpired(): Boolean
    fun expiredAt(): Date
}

private class UsableImpl : Usable {
    private var used = false

    override fun isUsed(): Boolean {
        return used
    }

    override fun setUsed() {
        used = true
    }
}

private class WillExpireImpl(
    val createdAtMillis: Long,
    val expireAfterMillis: Long,
) : WillExpire {
    override fun isExpired(): Boolean {
        return createdAtMillis + expireAfterMillis < Date().time
    }

    override fun expiredAt(): Date {
        return Date(createdAtMillis + expireAfterMillis)
    }
}

@Serializable
class OnceToken(
    @Serializable(with = UUIDSerializer::class)
    override val uuid: UUID,
) : Token(), Usable by UsableImpl() {

    override fun isValid(): Boolean {
        return isUsed()
    }

    override fun toString(): String {
        // TODO: I18n
        return "$uuid (can only be used once)"
    }
}

@Serializable
class TimedToken(
    @Serializable(with = UUIDSerializer::class)
    override val uuid: UUID,
    private val createdAtMillis: Long,
    private val expiredAfterMillis: Long
) : Token(), WillExpire by WillExpireImpl(createdAtMillis, expiredAfterMillis) {
    override fun isValid(): Boolean {
        return !isExpired()
    }

    override fun toString(): String {
        // TODO: I18n
        return "$uuid (valid before ${expiredAt()})"
    }
}

@Serializable
class TimedOnceToken(
    @Serializable(with = UUIDSerializer::class)
    override val uuid: UUID,
    private val createdAtMillis: Long,
    private val expiredAfterMillis: Long
) :
    Token(),
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

@Serializable
class ForeverToken(
    @Serializable(with = UUIDSerializer::class)
    override val uuid: UUID,
) : Token() {
    override fun isValid(): Boolean {
        return true
    }

    override fun toString(): String {
        return "$uuid (forever valid)"
    }
}