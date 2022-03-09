@file:Suppress("Unused")
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
package online.ruin_of_future.informative_mc_core

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.X509ExtensionUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit


fun getFile(root: String, path: String): File {
    return File("$root${File.separatorChar}$path")
}

fun getFile(absolutePath: String): File {
    return File(absolutePath)
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> saveToFileLocked(content: T, file: File) {
    val raFile = RandomAccessFile(file, "rw")
    val lock = raFile.channel.lock()
    val outputStream = ByteArrayOutputStream()
    Json.encodeToStream(content, outputStream)
    raFile.channel.write(ByteBuffer.wrap(outputStream.toByteArray()))
    lock.release()
    raFile.channel.close()
}

fun Long.humanReadableSize(): String {
    val num = this
    val kb = 1L shl 10
    val mb = 1L shl 20
    val gb = 1L shl 30
    val tb = 1L shl 40
    return if (num < kb) {
        "$num B"
    } else if (num < mb) {
        val f = String.format("%.2f", num.toDouble() / kb)
        "$f KB"
    } else if (num < gb) {
        val f = String.format("%.2f", num.toDouble() / mb)
        "$f MB"
    } else if (num < tb) {
        val f = String.format("%.2f", num.toDouble() / gb)
        "$f GB"
    } else {
        val f = String.format("%.2f", num.toDouble() / tb)
        "$f TB"
    }
}

fun Int.humanReadableSize(): String {
    return this.toLong().humanReadableSize()
}

fun generateRandomString(
    length: Int,
    candidateChars: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ~!@#$%^&*()_+=-0987654321`"
): String {
    val sb = StringBuilder()
    for (i in 0 until length) {
        sb.append(candidateChars.random())
    }
    return sb.toString()
}

fun generateKeyPair(keySize: Int): KeyPair {
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(keySize)
    return generator.generateKeyPair()
}

/**
 * Modified from
 * `https://github.com/misterpki/selfsignedcert/blob/master/src/main/java/com/misterpki/SelfSignedCertGenerator.java`
 * */
fun createSubjectKeyId(publicKey: PublicKey): SubjectKeyIdentifier {
    val publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.encoded)
    val digitCalc = BcDigestCalculatorProvider().get(AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1))
    return X509ExtensionUtils(digitCalc).createSubjectKeyIdentifier(publicKeyInfo)
}

/**
 * Modified from
 * `https://github.com/misterpki/selfsignedcert/blob/master/src/main/java/com/misterpki/SelfSignedCertGenerator.java`
 * */
fun createAuthorityKeyId(publicKey: PublicKey): AuthorityKeyIdentifier {
    val publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.encoded)
    val digitCalc = BcDigestCalculatorProvider().get(AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1))
    return X509ExtensionUtils(digitCalc).createAuthorityKeyIdentifier(publicKeyInfo)

}

/**
 * Modified from
 * `https://github.com/misterpki/selfsignedcert/blob/master/src/main/java/com/misterpki/SelfSignedCertGenerator.java`
 * */
fun generateCertificate(
    distinguishedName: String,
    keyPair: KeyPair,
    expireAfterDay: Long = 100000,
    sigAlgName: String = "SHA256withRSA",
): X509Certificate {
    val privateKey = keyPair.private
    val publicKey = keyPair.public
    val from = Date()
    val to = Date(from.time + TimeUnit.DAYS.toMillis(expireAfterDay))

    val contentSigner = JcaContentSignerBuilder(sigAlgName).build(privateKey)
    val x500Name = X500Name("CN=$distinguishedName")
    val randBytes = ByteArray(20)
    SecureRandom().nextBytes(randBytes)
    val certBuilder = JcaX509v3CertificateBuilder(
        x500Name,
        BigInteger(randBytes),
        from,
        to,
        x500Name,
        publicKey
    )
        .addExtension(Extension.subjectKeyIdentifier, false, createSubjectKeyId(publicKey))
        .addExtension(Extension.authorityKeyIdentifier, false, createAuthorityKeyId(publicKey))
        .addExtension(Extension.basicConstraints, true, BasicConstraints(true))

    return JcaX509CertificateConverter()
        .setProvider(BouncyCastleProvider())
        .getCertificate(certBuilder.build(contentSigner))
}