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
package online.ruin_of_future.informative_mc_core.web_api

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import kotlinx.serialization.ExperimentalSerializationApi
import online.ruin_of_future.informative_mc_core.auth.TokenManager
import online.ruin_of_future.informative_mc_core.config.ModConfig
import online.ruin_of_future.informative_mc_core.data.ModData
import online.ruin_of_future.informative_mc_core.tmpDirPath
import online.ruin_of_future.informative_mc_core.util.generateCertificate
import online.ruin_of_future.informative_mc_core.util.generateKeyPair
import online.ruin_of_future.informative_mc_core.web_api.handler.*
import org.apache.logging.log4j.LogManager
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.util.ssl.SslContextFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.KeyFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.spec.X509EncodedKeySpec


/**
 * Server which expose web api and potential web pages.
 * */
@Suppress("UnUsed")
@OptIn(ExperimentalSerializationApi::class)
class ApiServer(
    private val modConfig: ModConfig,
    private val modData: ModData,
    private val tokenManager: TokenManager,
) {
    private val LOGGER = LogManager.getLogger("IMC-Core")

    private val serverPort: Int = modConfig.port

    /**
     * If the cert and key files are provided in config, a temporary path will be used.
     * Otherwise, it would be read directly from config.
     * */
    private lateinit var keyStorePath: String
    private lateinit var keyPassword: String

    private var app: Javalin

    private val paramFreeHandlers = mutableMapOf<ApiID, ParamFreeHandler>()
    private val paramGetHandlers = mutableMapOf<ApiID, ParamGetHandler>()
    private val paramPostHandlers = mutableMapOf<ApiID, ParamPostHandler>()

    private fun registerApiHandler(apiHandler: ParamFreeHandler) {
        paramFreeHandlers.putIfAbsent(apiHandler.id, apiHandler)
    }

    private fun registerApiHandler(apiHandler: ParamGetHandler) {
        paramGetHandlers.putIfAbsent(apiHandler.id, apiHandler)
    }

    private fun registerApiHandler(apiHandler: ParamPostHandler) {
        paramPostHandlers.putIfAbsent(apiHandler.id, apiHandler)
    }

    private fun setupAllApi() {
        registerApiHandler(HeartbeatHandler())
        registerApiHandler(JvmInfoHandler(tokenManager, modData))
        registerApiHandler(OSInfoHandler(tokenManager, modData))
        registerApiHandler(PlayerInfoHandler(tokenManager, modData))
        registerApiHandler(UserRegisterHandler(tokenManager, modData))
        registerApiHandler(UserTestHandler(tokenManager, modData))

        // Late init
        paramFreeHandlers.forEach { (_, handler) ->
            handler.setup()
        }
        paramGetHandlers.forEach { (_, handler) ->
            handler.setup()
        }
        paramPostHandlers.forEach { (_, handler) ->
            handler.setup()
        }
    }

    private fun getSslContextFactory(): SslContextFactory.Server {
        val factory = SslContextFactory.Server()
        factory.keyStorePath = keyStorePath
        factory.setKeyStorePassword(keyPassword)
        return factory
    }

    private fun ensureKeyStore() {
        var flag = false
        var certFile: File? = null
        var keyFile: File? = null
        if (modConfig.certConfig != null) {
            certFile = File(modConfig.certConfig.certPath)
            keyFile = File(modConfig.certConfig.keyPath)
            flag = certFile.exists() && keyFile.exists()
        }
        if (flag) { // No cert and key files are provided in config
            keyStorePath = "$tmpDirPath${File.separatorChar}ICM-TMP.jks"
            keyPassword = modConfig.password
            val keyStoreFile = File(keyStorePath)
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())

            val certChain = arrayOf(
                CertificateFactory
                    .getInstance("RSA")
                    .generateCertificate(
                        certFile!!.inputStream()
                    )
            )
            val privateKey = KeyFactory
                .getInstance("RSA")
                .generatePrivate(X509EncodedKeySpec(keyFile!!.readBytes()))

            LOGGER.info("Detected cert file and key file. Using them to start server.")

            keyStore.setKeyEntry("api-server-secret", privateKey, keyPassword.toCharArray(), certChain)
            val os = keyStoreFile.outputStream()
            keyStore.store(os, keyPassword.toCharArray())
            os.close()
        } else {
            keyStorePath = modConfig.keyStoreConfig.keyStorePath
            keyPassword = modConfig.password
            val keyStoreFile = File(keyStorePath)
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            if (!keyStoreFile.exists()) {
                LOGGER.warn("No KeyStore found. Creating a new one at ${keyStoreFile.absolutePath}")
                LOGGER.warn("The creating procedure might take a long time.")
                keyStore.load(null, keyPassword.toCharArray())
                keyStoreFile.createNewFile()
                val keyPair = generateKeyPair(8192)
                val certChain = arrayOf(generateCertificate("localhost", keyPair))
                keyStore.setKeyEntry("api-server-secret", keyPair.private, keyPassword.toCharArray(), certChain)
                val os = keyStoreFile.outputStream()
                keyStore.store(os, keyPassword.toCharArray())
                os.close()
            } else {
                LOGGER.info("Detected KeyStore. Loading from ${keyStoreFile.absolutePath}")
                keyStore.load(keyStoreFile.inputStream(), keyPassword.toCharArray())
            }
        }
    }

    init {
        setupAllApi()
        ensureKeyStore()

        app = Javalin.create { config ->
            config.enableCorsForAllOrigins()
            config.enforceSsl = true
            config.server {
                val server = Server()
                val sslConnector = ServerConnector(server, getSslContextFactory())
                sslConnector.port = serverPort
                server.connectors = arrayOf(sslConnector)
                server
            }
        }.routes {
            path("api") {
                paramFreeHandlers.forEach { (id, handler) ->
                    get(id.toURIString()) { ctx ->
                        val outputStream = ByteArrayOutputStream()
                        ctx.contentType("application/json")
                        handler.handleRequest(outputStream)
                        ctx.result(outputStream.toByteArray())
                        outputStream.close()
                    }
                }
                paramGetHandlers.forEach { (id, handler) ->
                    get(id.toURIString()) { ctx ->
                        val outputStream = ByteArrayOutputStream()
                        ctx.contentType("application/json")
                        handler.handleRequest(ctx.queryParamMap(), outputStream)
                        ctx.result(outputStream.toByteArray())
                        outputStream.close()
                    }
                }
                paramPostHandlers.forEach { (id, handler) ->
                    post(id.toURIString()) { ctx ->
                        val outputStream = ByteArrayOutputStream()
                        ctx.contentType("application/json")
                        handler.handleRequest(ctx.formParamMap(), outputStream)
                        ctx.result(outputStream.toByteArray())
                        outputStream.close()
                    }
                }
            }
        }.start()
        LOGGER.info("IMC-Core is starting at port $serverPort")
    }
}