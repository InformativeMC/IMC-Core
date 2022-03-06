package online.ruin_of_future.informative_mc_core.web_api

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import kotlinx.serialization.ExperimentalSerializationApi
import online.ruin_of_future.informative_mc_core.ModConfig
import online.ruin_of_future.informative_mc_core.generateCertificate
import online.ruin_of_future.informative_mc_core.generateKeyPair
import online.ruin_of_future.informative_mc_core.web_api.handler.*
import org.apache.logging.log4j.LogManager
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.util.ssl.SslContextFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.KeyStore
import java.security.cert.X509Certificate


/**
 * Server which expose web api and potential web pages.
 * */
@OptIn(ExperimentalSerializationApi::class)
object ApiServer {
    private val LOGGER = LogManager.getLogger("IMC-Core")

    private val serverPort: Int
        get() = ModConfig.CURRENT.port

    // It should not be hard coded, but I don't know how.
    private const val keyPassword = "5rt6KQB09WIDpxm^GBPHCy%AL7keKa-u#rG~u\$8tdB#6juALHL"
    private lateinit var app: Javalin

    private val paramFreeHandlers = mutableMapOf<ApiID, ParamFreeHandler>()
    private val paramGetHandlers = mutableMapOf<ApiID, ParamGetHandler>()

    fun registerApiHandler(apiHandler: ParamFreeHandler) {
        paramFreeHandlers.putIfAbsent(apiHandler.id, apiHandler)
    }

    fun registerApiHandler(apiHandler: ParamGetHandler) {
        paramGetHandlers.putIfAbsent(apiHandler.id, apiHandler)
    }

    private fun setupAllApi() {
        registerApiHandler(Heartbeat())
        registerApiHandler(JvmInfo())
        registerApiHandler(OSInfo())
        registerApiHandler(PlayerInfo())

        // Late init
        paramFreeHandlers.forEach { (_, handler) ->
            handler.setup()
        }
        paramGetHandlers.forEach { (_, handler) ->
            handler.setup()
        }
    }

    private fun getSslContextFactory(): SslContextFactory.Server {
        val factory = SslContextFactory.Server()
        factory.keyStorePath = ModConfig.CURRENT.keyStorePath
        factory.setKeyStorePassword(keyPassword)
        return factory
    }

    private fun ensureKeyStore() {
        val keyStoreFile = File(ModConfig.CURRENT.keyStorePath)
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

    /**
     * Late init function, called when mod loaded
     * */
    fun setup() {
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
                        handler.handleRequest(outputStream)
                        ctx.result(outputStream.toByteArray())
                    }
                }
                paramGetHandlers.forEach { (id, handler) ->
                    get(id.toURIString()) { ctx ->
                        val outputStream = ByteArrayOutputStream()
                        handler.handleRequest(ctx.queryParamMap(), outputStream)
                        ctx.result(outputStream.toByteArray())
                    }
                }
            }
        }.start()
        LOGGER.info("IMC-Core is starting at port $serverPort")
    }
}