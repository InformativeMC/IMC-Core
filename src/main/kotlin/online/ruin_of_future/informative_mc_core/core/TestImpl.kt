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

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
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
        ApiTests(modDataManager).run()
    }

    private fun registerServerStartedCallback() {
        ServerLifecycleEvents.SERVER_STARTED.register { _ ->
            setupTmpAuth()
            runAllTest()
        }
    }

    override fun onInitialize() {
        super.onInitialize()
        LOGGER.warn("Using a test implementation of IMC-Core!")
        registerServerStartedCallback()
    }
}