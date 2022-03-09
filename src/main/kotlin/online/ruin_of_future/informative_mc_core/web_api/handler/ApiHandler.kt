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

package online.ruin_of_future.informative_mc_core.web_api.handler

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import online.ruin_of_future.informative_mc_core.web_api.ApiID
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
abstract class ApiHandler {
    abstract val id: ApiID

    // If you need to init something after mod loaded, override setup().
    fun setup() {}

    inline fun <reified T> T.writeToStream(outputStream: OutputStream) {
        Json.encodeToStream(this, outputStream)
    }
}

abstract class ParamFreeHandler : ApiHandler() {
    abstract fun handleRequest(outputStream: OutputStream)
}

abstract class ParamGetHandler : ApiHandler() {
    abstract fun handleRequest(
        queryParamMap: Map<String, List<String>>,
        outputStream: OutputStream
    )
}