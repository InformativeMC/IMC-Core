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
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import online.ruin_of_future.informative_mc_core.data.ModData

// TODO: fancy display in ChatHUD

class ImcCommand(
    modData: ModData,
    private val isTestImpl: Boolean
) {
    private val mainCmd = CommandManager.literal("imc")
    private val authCommand = ImcAuthCommand(modData.tmpAuthManager)
    private val testCommand = TestRun(modData)

    private fun buildAuth(): LiteralArgumentBuilder<ServerCommandSource>? {
        mainCmd.then(authCommand.build())
        return mainCmd
    }

    private fun buildTest(): LiteralArgumentBuilder<ServerCommandSource>? {
        mainCmd.then(testCommand.build())
        return mainCmd
    }

    fun setup() {
        CommandRegistrationCallback.EVENT.register { dispatcher, dedicated ->
            buildAuth()
            if (isTestImpl) {
                buildTest()
            }
            dispatcher.register(mainCmd)
        }
    }
}