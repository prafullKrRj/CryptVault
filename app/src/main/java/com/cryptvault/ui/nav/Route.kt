package com.cryptvault.ui.nav

object Route {
    const val Setup = "setup"
    const val Unlock = "unlock"
    const val Main = "main"
    const val VaultTab = "vault"
    const val GeneratorTab = "generator"
    const val SettingsTab = "settings"
    const val EntryDetail = "entry/{id}"
    const val AddEditEntry = "entry_edit?id={id}"
    fun entryDetail(id: Long) = "entry/$id"
    fun addEditEntry(id: Long? = null) = if (id == null) "entry_edit?id=-1" else "entry_edit?id=$id"
    const val BottomNavRoutes = "$VaultTab|$GeneratorTab|$SettingsTab"
}