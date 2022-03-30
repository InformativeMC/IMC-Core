package online.ruin_of_future.informative_mc_core.web_api.handler

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import online.ruin_of_future.informative_mc_core.data.ModData
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import online.ruin_of_future.informative_mc_core.web_api.id.GiveInventoryApiId
import online.ruin_of_future.informative_mc_core.web_api.response.GiveInventoryResponse
import online.ruin_of_future.informative_mc_core.web_api.response.GiveInventoryResponseDetail
import java.io.OutputStream

class GiveInventoryHandler(
    private val server: MinecraftServer,
    private val modData: ModData,
) : ParamPostHandler() {
    override val id: ApiId = GiveInventoryApiId

    override fun handleRequest(formParams: Map<String, List<String>>, outputStream: OutputStream) {
        val req = parseUserRequest(formParams)
        val response = if (!modData.userManager.hasUser(req.username)) {
            GiveInventoryResponse.usernameError(req.username)
        } else if (!modData.userManager.verifyUserToken(req.username, req.token)) {
            GiveInventoryResponse.invalidTokenError(req.token)
        } else {
            try {
                val target = formParams["target"]?.get(0)
                    ?.let { Json.decodeFromString<Array<String>>(it) }
                    ?.toSet()
                    ?: throw IllegalArgumentException("Need \"target\" parameter")
                val item = formParams["itemId"]?.get(0)
                    ?.let {
                        if (!it.contains(':')) {
                            "minecraft:$it"
                        } else {
                            it
                        }
                    }
                    ?.let { Identifier(it) }
                    ?.let {
                        val x = Registry.ITEM[it]
                        if (Registry.ITEM.getId(x) != it) {
                            throw IllegalArgumentException("Not a valid item id")
                        } else {
                            x
                        }
                    }
                    ?: throw IllegalArgumentException("Need a valid \"item\" parameter")
                val count = formParams["count"]?.get(0)
                    ?.toIntOrNull()
                    ?: throw IllegalArgumentException("Need a valid \"count\" parameter")
                server.playerManager.playerList
                    .filterNotNull()
                    .forEach {
                        if (target.isEmpty() || it.name.asString() in target) {
                            val itemStack = ItemStack(item, count)
                            it.giveItemStack(itemStack)
                        }
                    }
                GiveInventoryResponse.success(GiveInventoryResponseDetail())
            } catch (e: Throwable) {
                GiveInventoryResponse.error(e.message ?: "unknown error during giving inventory.")
            }
        }
        response.writeToStream(outputStream)
    }
}