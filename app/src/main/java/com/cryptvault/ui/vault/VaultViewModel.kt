package com.cryptvault.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptvault.domain.model.VaultEntry
import com.cryptvault.domain.usecase.CopyWithTTLUseCase
import com.cryptvault.domain.usecase.DeleteEntryUseCase
import com.cryptvault.domain.usecase.GetEntriesUseCase
import com.cryptvault.domain.usecase.GetEntryUseCase
import com.cryptvault.domain.usecase.AddEntryUseCase
import com.cryptvault.domain.usecase.UpdateEntryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VaultListState(
    val query: String = "",
    val entries: List<VaultEntry> = emptyList(),
    val loading: Boolean = true,
    val transientMessage: String? = null,
)

class VaultViewModel(
    private val getEntries: GetEntriesUseCase,
    private val getEntry: GetEntryUseCase,
    private val addEntry: AddEntryUseCase,
    private val updateEntry: UpdateEntryUseCase,
    private val deleteEntry: DeleteEntryUseCase,
    private val copy: CopyWithTTLUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val msg = MutableStateFlow<String?>(null)
    private val loading = MutableStateFlow(true)

    val state: StateFlow<VaultListState> = combine(
        getEntries(),
        query,
        msg,
        loading,
    ) { entries, q, m, l ->
        val filtered = if (q.isBlank()) entries
        else entries.filter { it.title.contains(q, ignoreCase = true) || it.username.contains(q, ignoreCase = true) }
        VaultListState(query = q, entries = filtered, loading = l, transientMessage = m)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, VaultListState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Trigger first emission
            getEntries().collect { loading.value = false; return@collect }
        }
    }

    fun onQueryChange(q: String) { query.value = q }

    fun save(entry: VaultEntry, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (entry.id == 0L) addEntry(entry) else updateEntry(entry)
                onDone()
            } catch (t: Throwable) {
                msg.value = t.message ?: "Save failed"
            }
        }
    }

    fun delete(entry: VaultEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                deleteEntry(entry)
                msg.value = "Deleted “${entry.title}”"
            } catch (t: Throwable) {
                msg.value = t.message ?: "Delete failed"
            }
        }
    }

    fun copyPassword(entry: VaultEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                copy(entry)
                msg.value = "Password copied — clears in 30s"
            } catch (t: Throwable) {
                msg.value = t.message ?: "Copy failed"
            }
        }
    }

    fun load(id: Long, onLoaded: (VaultEntry?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { onLoaded(getEntry(id)) }
    }

    fun consumeMessage() { msg.value = null }
}