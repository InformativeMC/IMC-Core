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
import online.ruin_of_future.informative_mc_core.data.ImcUser
import online.ruin_of_future.informative_mc_core.util.generateRandomString
import online.ruin_of_future.informative_mc_core.web_api.test.ApiTests

sealed class ImcCoreTestImpl : ImcCoreImpl() {
    override val isTestImpl: Boolean = true
    private fun setupTestUser(): ImcUser {
        LOGGER.info("Setting temporary tokens for tests...")
        return modData.userManager.addUser("TEST_${generateRandomString(5)}")
    }

    private fun runAllTest(testUser: ImcUser) {
        ApiTests(modData, testUser).run()
    }

    private fun registerServerStartedCallback() {
        ServerLifecycleEvents.SERVER_STARTED.register { _ ->
            val testUser = setupTestUser()
            runAllTest(testUser)
        }
    }

    override fun onInitialize() {
        LOGGER.warn("Using a test implementation of IMC-Core!")
        super.onInitialize()
        registerServerStartedCallback()
    }
}