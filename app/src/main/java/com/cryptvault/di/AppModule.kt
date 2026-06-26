package com.cryptvault.di

import androidx.room.Room
import com.cryptvault.data.db.CryptVaultDb
import com.cryptvault.data.prefs.SecurePrefs
import com.cryptvault.data.repository.SecureClipboard
import com.cryptvault.data.repository.SessionRepository
import com.cryptvault.data.repository.VaultRepository
import com.cryptvault.domain.usecase.AddEntryUseCase
import com.cryptvault.domain.usecase.CopyTextWithTTLUseCase
import com.cryptvault.domain.usecase.CopyWithTTLUseCase
import com.cryptvault.domain.usecase.DeleteEntryUseCase
import com.cryptvault.domain.usecase.GeneratePasswordUseCase
import com.cryptvault.domain.usecase.GetEntriesUseCase
import com.cryptvault.domain.usecase.GetEntryUseCase
import com.cryptvault.domain.usecase.LockVaultUseCase
import com.cryptvault.domain.usecase.SetupMasterPasswordUseCase
import com.cryptvault.domain.usecase.UnlockWithBiometricUseCase
import com.cryptvault.domain.usecase.UnlockWithPasswordUseCase
import com.cryptvault.domain.usecase.UpdateEntryUseCase
import com.cryptvault.domain.usecase.WipeVaultUseCase
import com.cryptvault.ui.generator.GeneratorViewModel
import com.cryptvault.ui.settings.SettingsViewModel
import com.cryptvault.ui.vault.VaultViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { SecurePrefs(androidContext()) }

    single {
        Room.databaseBuilder(
            androidContext(),
            CryptVaultDb::class.java,
            CryptVaultDb.DB_NAME,
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single { get<CryptVaultDb>().vaultDao() }

    single { SessionRepository(get()) }
    single { VaultRepository(get()) }
    single { SecureClipboard(androidContext(), get()) }

    factory { SetupMasterPasswordUseCase(get()) }
    factory { UnlockWithPasswordUseCase(get()) }
    factory { UnlockWithBiometricUseCase(get()) }
    factory { LockVaultUseCase(get()) }
    factory { WipeVaultUseCase(get(), get()) }

    factory { GetEntriesUseCase(get()) }
    factory { GetEntryUseCase(get()) }
    factory { AddEntryUseCase(get()) }
    factory { UpdateEntryUseCase(get()) }
    factory { DeleteEntryUseCase(get()) }

    factory { GeneratePasswordUseCase() }

    factory { CopyWithTTLUseCase(get(), get(), get()) }
    factory { CopyTextWithTTLUseCase(get()) }

    viewModel { VaultViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { GeneratorViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get()) }
}