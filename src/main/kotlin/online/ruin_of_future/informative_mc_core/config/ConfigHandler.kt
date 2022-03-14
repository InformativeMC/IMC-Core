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
package online.ruin_of_future.informative_mc_core.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object ConfigHandler {

    private val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    @Throws(SecurityException::class, IOException::class, JsonIOException::class)
    fun load(path: Path) {
        if (path.toFile().exists()) {
            Files.newBufferedReader(path).use { reader ->
                // TODO: load config
            }
        }
    }

    @Throws(JsonIOException::class, IOException::class)
    private fun save(path: Path) {
        Files.newBufferedWriter(path).use { writer ->
            // TODO: save config
        }
    }
}
