package online.ruin_of_future.informative_mc_core.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import online.ruin_of_future.informative_mc_core.token_system.TokenManager

// TODO: fancy display in ChatHUD

object ImcCommand {
    private fun build(tokenManager: TokenManager): LiteralArgumentBuilder<ServerCommandSource>? {
        val mainCmd = CommandManager.literal("imc")
        mainCmd.then(AuthCommand.build(tokenManager))
        return mainCmd
    }

    fun setup(tokenManager: TokenManager) {
        CommandRegistrationCallback.EVENT.register { dispatcher, dedicated ->
            dispatcher.register(build(tokenManager))
        }
    }
}