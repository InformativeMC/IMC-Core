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
import online.ruin_of_future.informative_mc_core.auth.TokenManager

// TODO: fancy display in ChatHUD

class ImcCommand(
    private val tokenManager: TokenManager
) {
    private val authCommand = ImcAuthCommand(tokenManager)

    private fun build(): LiteralArgumentBuilder<ServerCommandSource>? {
        val mainCmd = CommandManager.literal("imc")
        mainCmd.then(authCommand.build())
        return mainCmd
    }

    fun setup() {
        CommandRegistrationCallback.EVENT.register { dispatcher, dedicated ->
            dispatcher.register(build())
        }
    }
}