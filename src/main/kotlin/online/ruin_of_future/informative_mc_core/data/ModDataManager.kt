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

import online.ruin_of_future.informative_mc_core.auth.TokenManager
import org.apache.logging.log4j.LogManager

class ModDataManager(
    val userManager: UserManager,
    val tmpAuthManager: TokenManager
) {
    private val LOGGER = LogManager.getLogger("IMC Data")

    companion object {
        val DEFAULT = ModDataManager(
            userManager = UserManager(),
            tmpAuthManager = TokenManager(),
        )
    }
}