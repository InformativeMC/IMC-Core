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
package online.ruin_of_future.informative_mc_core.core

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.server.MinecraftServer
import online.ruin_of_future.informative_mc_core.auth.TimedOnceToken
import online.ruin_of_future.informative_mc_core.web_api.test.ApiTests
import java.util.concurrent.TimeUnit

sealed class ImcCoreTestImpl : ImcCoreImpl() {
    override val isTestImpl: Boolean = true
    private lateinit var tmpAuthTokens: List<TimedOnceToken>

    private fun setupTmpAuth(num: Int = 5) {
        LOGGER.info("Setting temporary tokens for tests...")
        val tokens = mutableListOf<TimedOnceToken>()
        for (i in 0 until num) {
            tokens.add(
                modDataManager.tmpAuthManager.addTimedOnceToken(
                    expiredAfterMillis = TimeUnit.MINUTES.toMillis(10)
                )
            )
        }
        tmpAuthTokens = tokens.toList()
    }

    private fun runAllTest() {
        runBlocking {
            delay(TimeUnit.SECONDS.toMillis(2))
            launch {
                ApiTests(modDataManager).runAll()
            }
        }
    }

    override fun mcServerCallback(server: MinecraftServer?) {
        super.mcServerCallback(server)
        setupTmpAuth()
        runAllTest()
    }

    override fun onInitialize() {
        LOGGER.warn("Using a test implementation of IMC-Core!")
        super.onInitialize()
    }
}