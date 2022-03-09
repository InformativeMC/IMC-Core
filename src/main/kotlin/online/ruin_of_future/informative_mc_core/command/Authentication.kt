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

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import online.ruin_of_future.informative_mc_core.token_system.TokenManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit


// TODO: can i show the tool tips even though mouse is not hovered?
private class AuthExpireSuggestionProvider : SuggestionProvider<ServerCommandSource> {
    override fun getSuggestions(
        context: CommandContext<ServerCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        builder.suggest("1m")
        builder.suggest("10m")
        builder.suggest("1h")
        builder.suggest("1d")
        builder.suggest("7d")
        return builder.buildFuture()
    }
}

object AuthCommand {
    private class IllegalTimeFormatException(msg: String) : Exception(msg)

    private fun getUnBuiltAuthCmd()
            : LiteralArgumentBuilder<ServerCommandSource> {
        return literal("auth")
            .requires { it.hasPermissionLevel(4) }!!
    }

    private fun appendForeverAuth(
        cmd: LiteralArgumentBuilder<ServerCommandSource>,
        tokenManager: TokenManager
    ) {
        cmd.then(
            literal("no-expire")
                .executes { ctx ->
                    val token = tokenManager.addForeverToken()
                    // TODO: I18n
                    val infoText = "just created a token"
                    ctx.source.sendFeedback(
                        LiteralText("${ctx.source.name}$infoText\n$token"),
                        false
                    )
                    return@executes 0
                }
        )
    }

    private fun String.parseTimeAsMillis(): Long {
        val unit = if (endsWith('s')) {
            TimeUnit.SECONDS
        } else if (endsWith('m')) {
            TimeUnit.MINUTES
        } else if (endsWith('h')) {
            TimeUnit.HOURS
        } else if (endsWith('d')) {
            TimeUnit.DAYS
        } else {
            throw IllegalTimeFormatException("Cannot parse $this as a valid time duration!")
        }
        val trimmed = substring(0, length - 1)
        trimmed.forEach {
            if (!it.isDigit()) {
                throw IllegalTimeFormatException("Cannot parse $this as a valid time duration!")
            }
        }
        return unit.toMillis(trimmed.toLong())
    }

    private fun appendTimedAuth(
        cmd: LiteralArgumentBuilder<ServerCommandSource>,
        tokenManager: TokenManager
    ) {
        cmd.then(
            literal("timed")
                .then(
                    argument("duration", StringArgumentType.string())!!
                        .suggests(AuthExpireSuggestionProvider())!!
                        .executes { ctx ->
                            try {
                                val t = StringArgumentType.getString(ctx, "duration").parseTimeAsMillis()
                                val token = tokenManager.addTimedToken(t)
                                // TODO: I18n
                                val infoText = "just created a token"
                                ctx.source.sendFeedback(
                                    LiteralText("${ctx.source.name}$infoText\n$token"),
                                    false
                                )
                                return@executes 0
                            } catch (e: IllegalTimeFormatException) {
                                ctx.source.sendFeedback(
                                    LiteralText(e.message),
                                    false
                                )
                                return@executes -1
                            }
                        }
                )
        )
    }

    private fun appendOnceAuth(
        cmd: LiteralArgumentBuilder<ServerCommandSource>,
        tokenManager: TokenManager
    ) {
        cmd.then(
            literal("once")
                .executes { ctx ->
                    val token = tokenManager.addOnceToken()
                    // TODO: I18n
                    val infoText = "just created a token"
                    ctx.source.sendFeedback(
                        LiteralText("${ctx.source.name}$infoText\n$token"),
                        false
                    )
                    return@executes 0
                }
        )
    }

    fun build(tokenManager: TokenManager): LiteralArgumentBuilder<ServerCommandSource> {
        val cmd = getUnBuiltAuthCmd()
        appendOnceAuth(cmd, tokenManager)
        appendForeverAuth(cmd, tokenManager)
        appendTimedAuth(cmd, tokenManager)
        return cmd
    }
}