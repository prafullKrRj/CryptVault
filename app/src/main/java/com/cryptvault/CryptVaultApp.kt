package com.cryptvault

import android.app.Application
import androidx.work.Configuration
import com.cryptvault.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class CryptVaultApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@CryptVaultApp)
            modules(appModule)
        }
        com.cryptvault.data.crypto.KeystoreManager.ensureMasterKeyExists()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}