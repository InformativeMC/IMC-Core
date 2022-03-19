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

import kotlinx.serialization.Serializable
import online.ruin_of_future.informative_mc_core.core.modConfigDirPath
import online.ruin_of_future.informative_mc_core.util.generateRandomString
import java.io.File

@Serializable
class ModConfig private constructor(
    val port: Int,
    val password: String,
    val keyStoreConfig: KeyStoreConfig,
    val certConfig: CertConfig?,
) {
    companion object {
        val DEFAULT = ModConfig(
            port = 3030,
            password = generateRandomString(50),
            keyStoreConfig = KeyStoreConfig(
                keyStorePath = "$modConfigDirPath${File.separatorChar}IMC-Core.jks",
            ),
            certConfig = null,
        )
    }
}