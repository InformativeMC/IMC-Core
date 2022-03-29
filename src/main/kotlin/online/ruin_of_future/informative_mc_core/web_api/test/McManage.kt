package online.ruin_of_future.informative_mc_core.web_api.test

import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import online.ruin_of_future.informative_mc_core.web_api.id.ApiId
import online.ruin_of_future.informative_mc_core.web_api.id.GameMessageApiId
import online.ruin_of_future.informative_mc_core.web_api.id.PlayerStatApiId
import online.ruin_of_future.informative_mc_core.web_api.response.ApiResponse
import online.ruin_of_future.informative_mc_core.web_api.response.GameMessageResponse
import online.ruin_of_future.informative_mc_core.web_api.response.PlayerStatResponse
import java.util.*
import java.util.concurrent.TimeUnit

private sealed class PlayStatTestBase(
    username: String,
    tokenUUID: UUID,
    target: Array<String> = arrayOf(), // Empty array capture all targets,
    operation: String,
    arg: Array<String>,
) : ApiTest<PlayerStatResponse> by PostApiTestImpl(
    PlayerStatApiId,
    username,
    tokenUUID,
    PlayerStatResponse.serializer(),
    mapOf(
        "target" to Json.encodeToString(target),
        "operation" to operation,
        "arg" to Json.encodeToString(arg),
    ),
) {
    override fun checkResponse(response: PlayerStatResponse) {
        super.checkResponse(response)
        assert(response.responseDetail?.players?.isNotEmpty() == true) {
            "No player in server!!!"
        }
    }
}

private const val healthTestVal = 10.0f

private class PlayerStatWatchTest(
    username: String,
    tokenUUID: UUID,
) : PlayStatTestBase(
    username,
    tokenUUID,
    operation = "watch",
    arg = arrayOf()
)

private class PlayerStatDamageTest(
    username: String,
    tokenUUID: UUID,
    private val originHealth: Map<String, Float>, // Health value of each user before damage.
) : PlayStatTestBase(
    username,
    tokenUUID,
    operation = "damage",
    arg = arrayOf(healthTestVal.toString())
) {
    override fun checkResponse(response: PlayerStatResponse) {
        super.checkResponse(response)
        response.responseDetail?.players?.forEach {
            assert(
                it.health < originHealth[it.name]!! &&
                        it.health + healthTestVal >= originHealth[it.name]!!
            ) {
                "Incorrect health after damage"
            }
        }
    }
}

private class PlayerStatHealTest(
    username: String,
    tokenUUID: UUID,
    private val originHealth: Map<String, Float>, // Health value of each user before heal.
) : PlayStatTestBase(
    username,
    tokenUUID,
    operation = "heal",
    arg = arrayOf(healthTestVal.toString())
) {
    override fun checkResponse(response: PlayerStatResponse) {
        super.checkResponse(response)
        assert(response.responseDetail?.players != null)
        response.responseDetail?.players?.forEach {
            assert(it.health >= originHealth[it.name]!! + healthTestVal) {
                "Incorrect health after heal"
            }
        }
    }
}

private class GameMessageTest(
    private val username: String,
    private val tokenUUID: UUID,
    target: Array<String> = arrayOf(), // Empty array capture all targets,
    message: String,
    actionBar: Boolean,
) : ApiTest<GameMessageResponse> by PostApiTestImpl(
    GameMessageApiId,
    username,
    tokenUUID,
    GameMessageResponse.serializer(),
    mapOf(
        "target" to Json.encodeToString(target),
        "message" to message,
        "actionBar" to Json.encodeToString(actionBar),
    )
)

class McManageTestBatch(
    private val username: String,
    private val tokenUUID: UUID,
) : ApiTestBatch {
    override val passedTest = mutableMapOf<ApiId, ApiResponse<*>>()
    override val failedTest = mutableMapOf<ApiId, Throwable>()
    override val name = "MC Manage"

    override suspend fun runWithCallback(
        onSuccess: (passedTest: Map<ApiId, ApiResponse<*>>) -> Unit,
        onFailure: (failedTest: Map<ApiId, Throwable>) -> Unit
    ) {
        var originHealth: Map<String, Float> = mapOf()
        var flag = true
        PlayerStatWatchTest(username, tokenUUID).runWithCallback(
            onSuccess = {
                originHealth = it.responseDetail?.players?.associate { player ->
                    player.name to player.health
                } ?: mapOf()
            },
            onFailure = {
                flag = false
                failedTest[PlayerStatApiId] = it
            }
        )
        if (flag) {
            delay(TimeUnit.SECONDS.toMillis(3))
            PlayerStatDamageTest(username, tokenUUID, originHealth).runWithCallback(
                onSuccess = {
                    originHealth = it.responseDetail?.players?.associate { player ->
                        player.name to player.health
                    } ?: mapOf()
                },
                onFailure = {
                    flag = false
                    failedTest[PlayerStatApiId] = it
                }
            )
        }
        if (flag) {
            delay(TimeUnit.SECONDS.toMillis(3))
            PlayerStatHealTest(username, tokenUUID, originHealth).runWithCallback(
                onSuccess = {
                    passedTest[PlayerStatApiId] = it
                },
                onFailure = {
                    flag = false
                    failedTest[PlayerStatApiId] = it
                }
            )
        }
        if (flag) {
            delay(TimeUnit.SECONDS.toMillis(3))
            GameMessageTest(
                username,
                tokenUUID,
                arrayOf(),
                "Hello %[username]%! It's %[date]% %[time]%",
                false
            )
                .runWithCallback(
                    onSuccess = {
                        passedTest[GameMessageApiId] = it
                    },
                    onFailure = {
                        failedTest[GameMessageApiId] = it
                    }
                )
        }
        if (flag) {
            delay(TimeUnit.SECONDS.toMillis(3))
            GameMessageTest(
                username,
                tokenUUID,
                arrayOf(),
                "Hello %[username]%! It's %[date]% %[time]%",
                true
            )
                .runWithCallback(
                    onSuccess = {
                        passedTest[GameMessageApiId] = it
                    },
                    onFailure = {
                        failedTest[GameMessageApiId] = it
                    }
                )
        }
        onSuccess(passedTest)
        onFailure(failedTest)
    }
}