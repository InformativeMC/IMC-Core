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
@file:Suppress("Unused")

package online.ruin_of_future.informative_mc_core.util

fun Long.humanReadableSize(): String {
    val num = this
    val kb = 1L shl 10
    val mb = 1L shl 20
    val gb = 1L shl 30
    val tb = 1L shl 40
    return if (num < kb) {
        "$num B"
    } else if (num < mb) {
        val f = String.format("%.2f", num.toDouble() / kb)
        "$f KB"
    } else if (num < gb) {
        val f = String.format("%.2f", num.toDouble() / mb)
        "$f MB"
    } else if (num < tb) {
        val f = String.format("%.2f", num.toDouble() / gb)
        "$f GB"
    } else {
        val f = String.format("%.2f", num.toDouble() / tb)
        "$f TB"
    }
}

fun Int.humanReadableSize(): String {
    return this.toLong().humanReadableSize()
}

fun generateRandomString(
    length: Int,
    candidateChars: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ~!@#$%^&*()_+=-0987654321"
): String {
    val sb = StringBuilder()
    for (i in 0 until length) {
        sb.append(candidateChars.random())
    }
    return sb.toString()
}