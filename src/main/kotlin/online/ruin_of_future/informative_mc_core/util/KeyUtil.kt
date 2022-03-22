package online.ruin_of_future.informative_mc_core.util

import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.X509ExtensionUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit

fun generateKeyPair(keySize: Int): KeyPair {
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(keySize)
    return generator.generateKeyPair()
}

/**
 * Modified from
 * [](https://github.com/misterpki/selfsignedcert/blob/master/src/main/java/com/misterpki/SelfSignedCertGenerator.java)
 * */
fun createSubjectKeyId(publicKey: PublicKey): SubjectKeyIdentifier {
    val publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.encoded)
    val digitCalc = BcDigestCalculatorProvider().get(AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1))
    return X509ExtensionUtils(digitCalc).createSubjectKeyIdentifier(publicKeyInfo)
}

/**
 * Modified from
 * [](https://github.com/misterpki/selfsignedcert/blob/master/src/main/java/com/misterpki/SelfSignedCertGenerator.java)
 * */
fun createAuthorityKeyId(publicKey: PublicKey): AuthorityKeyIdentifier {
    val publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.encoded)
    val digitCalc = BcDigestCalculatorProvider().get(AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1))
    return X509ExtensionUtils(digitCalc).createAuthorityKeyIdentifier(publicKeyInfo)

}

/**
 * Modified from
 * [](https://github.com/misterpki/selfsignedcert/blob/master/src/main/java/com/misterpki/SelfSignedCertGenerator.java)
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