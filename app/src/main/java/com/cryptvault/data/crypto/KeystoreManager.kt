package com.cryptvault.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages the AES-256 master key inside the Android KeyStore.
 * Hardware-backed on devices that support it. Survives reboot.
 */
object KeystoreManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    const val MASTER_KEY_ALIAS = "cryptvault_master_key_v1"

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    fun getOrCreateMasterKey(): SecretKey {
        keyStore.getKey(MASTER_KEY_ALIAS, null)?.let { return it as SecretKey }
        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE,
        )
        val spec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    fun ensureMasterKeyExists() {
        getOrCreateMasterKey()
    }

    fun deleteMasterKey() {
        if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            keyStore.deleteEntry(MASTER_KEY_ALIAS)
        }
    }

    /** Pack IV + ciphertext into one blob. */
    fun packBlob(iv: ByteArray, cipherText: ByteArray): ByteArray =
        ByteBuffer.allocate(4 + iv.size + cipherText.size).apply {
            putInt(iv.size)
            put(iv)
            put(cipherText)
        }.array()

    fun unpackBlob(blob: ByteArray): Pair<ByteArray, ByteArray> {
        val buf = ByteBuffer.wrap(blob)
        val ivLen = buf.int
        val iv = ByteArray(ivLen).also { buf.get(it) }
        val ct = ByteArray(buf.remaining()).also { buf.get(it) }
        return iv to ct
    }
}

object CipherEngine {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_BITS = 128

    fun encrypt(key: SecretKey, plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val ct = cipher.doFinal(plaintext)
        return KeystoreManager.packBlob(iv, ct)
    }

    fun decrypt(key: SecretKey, blob: ByteArray): ByteArray {
        val (iv, ct) = KeystoreManager.unpackBlob(blob)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(ct)
    }
}