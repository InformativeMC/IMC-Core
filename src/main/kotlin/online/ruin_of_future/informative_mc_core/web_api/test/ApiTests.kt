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
package online.ruin_of_future.informative_mc_core.web_api.test

import kotlinx.coroutines.*
import online.ruin_of_future.informative_mc_core.data.ImcUser
import online.ruin_of_future.informative_mc_core.data.ModDataManager
import online.ruin_of_future.informative_mc_core.util.ConsoleLine
import online.ruin_of_future.informative_mc_core.util.ConsoleLineSegment
import online.ruin_of_future.informative_mc_core.util.VirtualConsoleOption
import online.ruin_of_future.informative_mc_core.util.boxedConsoleString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Test class of all unit tests.
 * It is a class instead of an object
 * because the tests might be called multiple times.
 * Tests in different batches are executed in parallel.
 */
class ApiTests(
    modDataManager: ModDataManager,
    testUser: ImcUser,
) {

    companion object {
        @JvmStatic
        private val LOGGER: Logger = LogManager.getLogger("IMC API Test")
    }

    private val tests = listOf(
        SystemInfoTestBatch(testUser.username, testUser.userToken.uuid),
        ImcManageTestBatch(modDataManager.tmpAuthManager.addTimedOnceToken().uuid),
        McManageTestBatch(testUser.username, testUser.userToken.uuid),
    )

    private val passTestNum = AtomicInteger(0)
    private val failedTestNum = AtomicInteger(0)

    private suspend fun runImpl(): Boolean = coroutineScope {
        tests.map {
            async {
                val batchResultLines = mutableListOf<ConsoleLine>()
                batchResultLines.add(
                    ConsoleLineSegment(
                        "[${it.name}]",
                        VirtualConsoleOption.Magenta,
                    ).asSingleLine(true)
                )
                it.runWithCallback(
                    onSuccess = {
                        passTestNum.addAndGet(it.size)
                        if (it.isNotEmpty()) {
                            val lines = mutableListOf<ConsoleLine>()
                            lines.add(
                                ConsoleLineSegment(
                                    "[Passed]",
                                    VirtualConsoleOption.Green,
                                ).asSingleLine()
                            )
                            it.forEach { (id, _) ->
                                lines.add(
                                    ConsoleLineSegment(
                                        id.toURIString(),
                                        VirtualConsoleOption.Cyan,
                                    ).asSingleLine()
                                )
                            }
                            lines.forEach { line ->
                                batchResultLines.add(line)
                            }
                        }
                    },
                    onFailure = {
                        failedTestNum.addAndGet(it.size)
                        if (it.isNotEmpty()) {
                            val lines = mutableListOf<ConsoleLine>()
                            lines.add(
                                ConsoleLineSegment(
                                    "[Failed]",
                                    VirtualConsoleOption.Red,
                                ).asSingleLine()
                            )
                            it.forEach { (k, v) ->
                                val line = ConsoleLine()
                                line.add(
                                    ConsoleLineSegment(
                                        k.toURIString(),
                                        VirtualConsoleOption.Yellow,
                                    )
                                )
                                line.add(
                                    ConsoleLineSegment(
                                        " <- ${v.message}",
                                        VirtualConsoleOption.BrightYellow,
                                    )
                                )
                                lines.add(line)
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
    fun run() {
        GlobalScope.launch(newFixedThreadPoolContext(5, "IMC-API-Test")) {
            // wait until API server started
            delay(TimeUnit.SECONDS.toMillis(3))
            runImpl()
        }
    }
}