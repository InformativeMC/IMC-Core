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
    lateinit var testUser: ImcUser
    private fun setupTestUser() {
        LOGGER.info("Setting temporary tokens for tests...")
        testUser = modDataManager.userManager.addUser("TEST_${generateRandomString(5)}")
    }

    private fun runAllTest() {
        ApiTests(modDataManager, testUser).run()
    }

    private fun registerServerStartedCallback() {
        ServerLifecycleEvents.SERVER_STARTED.register { _ ->
            setupTestUser()
            runAllTest()
        }
    }

    override fun onInitialize() {
        super.onInitialize()
        LOGGER.warn("Using a test implementation of IMC-Core!")
        registerServerStartedCallback()
    }
}