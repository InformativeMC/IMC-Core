package online.ruin_of_future.informative_mc_core.web_api.test

import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


/**
 * Allow untrusted certificates. Copied from `https://stackoverflow.com/a/59322754/14172056`.
 * */
fun OkHttpClient.Builder.ignoreAllSSLErrors(): OkHttpClient.Builder {
    val naiveTrustManager = object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
    }

    val insecureSocketFactory = SSLContext.getInstance("TLSv1.2").apply {
        val trustAllCerts = arrayOf<TrustManager>(naiveTrustManager)
        init(null, trustAllCerts, SecureRandom())
    }.socketFactory

    sslSocketFactory(insecureSocketFactory, naiveTrustManager)
    hostnameVerifier { _, _ -> true }
    return this
}

fun String.smartAppendApiAddress(another: String): String {
    val head = if (this.endsWith('/')) {
        this.substring(0, this.length - 1)
    } else {
        this
    }
    val tail1 = if (another.startsWith('/')) {
        another.substring(1, another.length)
    } else {
        another
    }
    val tail2 = if (tail1.startsWith("api")) {
        tail1
    } else {
        "api/$tail1"
    }
    return "$head/$tail2"
}