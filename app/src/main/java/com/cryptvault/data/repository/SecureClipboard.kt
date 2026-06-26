package com.cryptvault.data.repository

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import com.cryptvault.data.prefs.SecurePrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Copies sensitive text to clipboard with auto-clear.
 * Flags the clip as sensitive (Android 13+) so the system hides preview.
 */
class SecureClipboard(
    context: Context,
    private val prefs: SecurePrefs,
) {

    private val appContext = context.applicationContext
    private val cm = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var clearJob: Job? = null

    fun copy(text: String, label: String = "CryptVault") {
        val clip = ClipData.newPlainText(label, text).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.description.extras = PersistableBundle().apply {
                    putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                }
            }
        }
        cm.setPrimaryClip(clip)
        scheduleClear()
    }

    private fun scheduleClear() {
        clearJob?.cancel()
        clearJob = scope.launch {
            val ttl = prefs.clipboardTtlSeconds.coerceAtLeast(5).toLong()
            delay(ttl * 1000L)
            clearIfOurs()
        }
    }

    private fun clearIfOurs() {
        val current = cm.primaryClip ?: return
        if (current.itemCount == 0) return
        val text = current.getItemAt(0).text?.toString().orEmpty()
        if (text.isEmpty()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cm.clearPrimaryClip()
        } else {
            cm.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }

    fun cancel() {
        clearJob?.cancel()
        clearJob = null
    }
}