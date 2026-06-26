package com.cryptvault.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Single source for EncryptedSharedPreferences backed by an AndroidKeystore master key.
 * Stores: PBKDF2 salt, verifier hash, settings. Excludes from backups.
 */
class SecurePrefs(context: Context) {

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context.applicationContext,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun isInitialized(): Boolean =
        prefs.contains(KEY_SALT) && prefs.contains(KEY_VERIFIER)

    fun writeMasterMaterial(salt: ByteArray, verifier: ByteArray) {
        prefs.edit()
            .putString(KEY_SALT, salt.toBase64())
            .putString(KEY_VERIFIER, verifier.toBase64())
            .apply()
    }

    fun readSalt(): ByteArray? = prefs.getString(KEY_SALT, null)?.fromBase64()
    fun readVerifier(): ByteArray? = prefs.getString(KEY_VERIFIER, null)?.fromBase64()

    var clipboardTtlSeconds: Int
        get() = prefs.getInt(KEY_CLIPBOARD_TTL, 30)
        set(value) { prefs.edit().putInt(KEY_CLIPBOARD_TTL, value).apply() }

    var autoLockSeconds: Int
        get() = prefs.getInt(KEY_AUTO_LOCK, 60)
        set(value) { prefs.edit().putInt(KEY_AUTO_LOCK, value).apply() }

    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, false)
        set(value) { prefs.edit().putBoolean(KEY_BIOMETRIC, value).apply() }

    fun wipe() {
        prefs.edit().clear().apply()
    }

    private fun ByteArray.toBase64(): String = android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)
    private fun String.fromBase64(): ByteArray = android.util.Base64.decode(this, android.util.Base64.NO_WRAP)

    companion object {
        const val FILE_NAME = "cryptvault_secure_prefs"
        private const val KEY_SALT = "salt_v1"
        private const val KEY_VERIFIER = "verifier_v1"
        private const val KEY_CLIPBOARD_TTL = "clipboard_ttl"
        private const val KEY_AUTO_LOCK = "auto_lock"
        private const val KEY_BIOMETRIC = "biometric_enabled"
    }
}