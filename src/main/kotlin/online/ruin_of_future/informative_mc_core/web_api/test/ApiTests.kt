package online.ruin_of_future.informative_mc_core.web_api.test

import kotlinx.coroutines.*
import online.ruin_of_future.informative_mc_core.data.ModDataManager
import online.ruin_of_future.informative_mc_core.util.VirtualConsoleOption
import online.ruin_of_future.informative_mc_core.util.boxedConsoleString
import online.ruin_of_future.informative_mc_core.util.inConsole
import org.apache.logging.log4j.LogManager
import java.util.concurrent.atomic.AtomicInteger

/**
 * Make it a class instead of an object.
 * Because the tests might be called multiple times.
 * Tests in different `ApiTestBatch` are executed in parallel.
 */
class ApiTests(
    modDataManager: ModDataManager
) {
    private val LOGGER = LogManager.getLogger("IMC API Test")
    private val tests = listOf<ApiTestBatch>(
        HeartbeatTestBatch(),
        UserTestBatch(modDataManager.tmpAuthManager.addTimedOnceToken().uuid),
    )

    private val passTestNum = AtomicInteger(0)
    private val failedTestNum = AtomicInteger(0)

    private suspend fun runImpl(): Boolean = coroutineScope {
        tests.map {
            async {
                val batchResultLines = mutableListOf<String>()
                batchResultLines.add("[${it.name}]".inConsole(VirtualConsoleOption.Centered))
                it.runWithCallback(
                    onSuccess = {
                        passTestNum.addAndGet(it.size)
                        if (it.isNotEmpty()) {
                            val lines = mutableListOf<String>()
                            lines.add("[Passed]")
                            it.forEach { (id, _) ->
                                lines.add("    ${id.toURIString()}")
                            }
                            lines.forEach { line ->
                                batchResultLines.add(line)
                            }
                        }
                    },
                    onFailure = {
                        failedTestNum.addAndGet(it.size)
                        if (it.isNotEmpty()) {
                            val lines = mutableListOf<String>()
                            lines.add("[Failed]")
                            it.forEach { (k, v) ->
                                lines.add("\t${k.toURIString()} <- ${v.message}")
                            }
                            lines.forEach { line ->
                                batchResultLines.add(line)
                            }
                        }
                    },
                )
                return@async batchResultLines
            }
        }.forEach {
            LOGGER.info("\n${boxedConsoleString(it.await())}")
        }
        val totalNum = passTestNum.get() + failedTestNum.get()
        val ratio = passTestNum.get().toDouble() * 100.0 / totalNum
        LOGGER.warn(
            "Finished $totalNum test(s). " +
                    "${passTestNum.get()} passed, ${failedTestNum.get()} failed. " +
                    "(${"%.2f".format(ratio)}%)"
        )
        return@coroutineScope failedTestNum.get() == 0
    }

    @OptIn(ObsoleteCoroutinesApi::class, DelicateCoroutinesApi::class)
    fun run() = runBlocking {
        GlobalScope.launch(newFixedThreadPoolContext(5, "IMC-API-Test")) {
            runImpl()
        }
    }
}