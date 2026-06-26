package com.cryptvault.data.crypto

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Derives a 256-bit key from a master password using PBKDF2-HmacSHA256.
 * 120,000 iterations, 32-byte random salt. Slow enough to resist brute-force,
 * fast enough to unlock in <500ms on low-end devices.
 */
object PasswordDerivation {

    private const val ALGO = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_BITS = 256
    const val SALT_BYTES = 32
    const val DERIVED_KEY_BYTES = 32

    private val rng = SecureRandom()

    fun generateSalt(): ByteArray = ByteArray(SALT_BYTES).also { rng.nextBytes(it) }

    fun derive(password: CharArray, salt: ByteArray, iterations: Int = ITERATIONS): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGO).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var r = 0
        for (i in a.indices) r = r or (a[i].toInt() xor b[i].toInt())
        return r == 0
    }

    fun randomBytes(n: Int): ByteArray = ByteArray(n).also { rng.nextBytes(it) }
}