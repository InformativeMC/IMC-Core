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
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import online.ruin_of_future.informative_mc_core.auth.TokenManager

class ImcAuthCommand(
    private val tmpAuthManager: TokenManager
) {
    private fun getUnBuiltAuthCmd()
            : LiteralArgumentBuilder<ServerCommandSource> {
        return literal("auth")
            .requires { it.hasPermissionLevel(4) }!!
    }

    private fun appendTimedOnceAuth(
        cmd: LiteralArgumentBuilder<ServerCommandSource>,
    ) {
        cmd.executes { ctx ->
            val token = tmpAuthManager.addTimedOnceToken()
            // TODO: I18n
            val infoText = "just created a token"
            ctx.source.sendFeedback(
                LiteralText("${ctx.source.name}$infoText\n$token"),
                false
            )
            return@executes 0
        }
    }

    fun build(): LiteralArgumentBuilder<ServerCommandSource> {
        val cmd = getUnBuiltAuthCmd()
        appendTimedOnceAuth(cmd)
        return cmd
    }
}