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
package online.ruin_of_future.informative_mc_core.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import online.ruin_of_future.informative_mc_core.data.ModDataManager
import online.ruin_of_future.informative_mc_core.web_api.test.ApiTests

class TestRun(
    private val modDataManager: ModDataManager
) {
    fun build(): LiteralArgumentBuilder<ServerCommandSource> {
        return CommandManager.literal("test")
            .executes {
                runBlocking {
                    launch {
                        ApiTests(modDataManager).runAll()
                    }
                }
                return@executes 0
            }
    }
}