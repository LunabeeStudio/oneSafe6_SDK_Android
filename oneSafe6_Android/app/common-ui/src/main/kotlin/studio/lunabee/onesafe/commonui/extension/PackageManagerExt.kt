package studio.lunabee.onesafe.commonui.extension

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import studio.lunabee.onesafe.domain.utils.ShaEngine
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.cert.CertificateFactory

/**
 * @see PackageManager.getPackageInfo
 * @see <a href="https://issuetracker.google.com/issues/246845196">Issue 246845196</a>
 */
fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int): PackageInfo? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            @Suppress("WrongConstant")
            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            getPackageInfo(packageName, flags)
        }
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
}

/**
 * Signature type is set to [PackageManager.CERT_INPUT_SHA256]
 *
 * @see PackageManager.hasSigningCertificate
 */
fun PackageManager.hasSigningCertificateCompat(callingPackage: String, signature: ByteArray): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        hasSigningCertificate(callingPackage, signature, PackageManager.CERT_INPUT_SHA256)
    } else {
        @Suppress("DEPRECATION")
        val callerSignature = getPackageInfoCompat(callingPackage, PackageManager.GET_SIGNATURES)
            ?.signatures
            ?.last()
            ?.let { getFingerprint(it) }

        callerSignature.contentEquals(signature)
    }
}

/**
 * Get fingerprint from a certificate in android.content.pm.Signature
 * @return String fingerprint that contains the SHA-256 digest
 *
 * https://gist.github.com/chinalwb/d546334ee8c5ba7afbad8a79d1e6a70f
 */
private fun getFingerprint(ce: Signature): ByteArray {
    val input: InputStream = ByteArrayInputStream(ce.toByteArray())
    val certificate = CertificateFactory.getInstance("X509").generateCertificate(input)
    return ShaEngine().sha256(certificate.encoded)
}
