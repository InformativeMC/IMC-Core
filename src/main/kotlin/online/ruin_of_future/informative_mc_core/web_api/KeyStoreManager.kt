package online.ruin_of_future.informative_mc_core.web_api

import online.ruin_of_future.informative_mc_core.config.ModConfig
import online.ruin_of_future.informative_mc_core.core.tmpDirPath
import online.ruin_of_future.informative_mc_core.util.generateCertificate
import online.ruin_of_future.informative_mc_core.util.generateKeyPair
import org.apache.logging.log4j.LogManager
import org.eclipse.jetty.util.ssl.SslContextFactory
import java.io.File
import java.security.KeyFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.spec.X509EncodedKeySpec

sealed class KeyStoreManager(
    private val modConfig: ModConfig,
) {
    private val LOGGER = LogManager.getLogger("IMC-Core")

    /**
     * If the cert and key files are provided in config, a temporary path will be used.
     * Otherwise, it would be read directly from config.
     * */
    protected lateinit var keyStorePath: String
    protected lateinit var keyPassword: String

    protected fun getSslContextFactory(): SslContextFactory.Server {
        val factory = SslContextFactory.Server()
        factory.keyStorePath = keyStorePath
        factory.setKeyStorePassword(keyPassword)
        return factory
    }

    protected fun ensureKeyStore() {
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
            val protectedKey = KeyFactory
                .getInstance("RSA")
                .generatePrivate(X509EncodedKeySpec(keyFile!!.readBytes()))

            LOGGER.info("Detected cert file and key file. Using them to start server.")

            keyStore.setKeyEntry("api-server-secret", protectedKey, keyPassword.toCharArray(), certChain)
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
}