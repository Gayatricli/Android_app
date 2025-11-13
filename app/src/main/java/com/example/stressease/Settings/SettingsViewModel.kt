package com.example.stressease.Settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SettingsRepository(application.applicationContext)
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(SettingsModel())
    val uiState: StateFlow<SettingsModel> = _uiState.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    init {
        // try to load from Firestore then fallback to local
        viewModelScope.launch {
            _loading.value = true
            val remote = repo.loadFromFirestoreOnce()
            if (remote != null) {
                _uiState.value = remote
                repo.saveToLocal(remote)
            } else {
                // fallback to local cache
                val local = repo.loadFromLocal()
                _uiState.value = local
            }
            _loading.value = false
        }
    }

    fun refreshFromCloud() {
        viewModelScope.launch {
            _loading.value = true
            val remote = repo.loadFromFirestoreOnce()
            if (remote != null) {
                _uiState.value = remote
                repo.saveToLocal(remote)
                _message.value = "Synced from cloud"
            } else {
                _message.value = "No cloud data found"
            }
            _loading.value = false
        }
    }

    fun updateAndSave(updated: SettingsModel) {
        viewModelScope.launch {
            _loading.value = true
            // Write to Firestore (primary)
            val ok = repo.saveToFirestore(updated)
            if (ok) {
                // update local cache
                repo.saveToLocal(updated)
                _uiState.value = updated.copy(lastSyncedMillis = System.currentTimeMillis())
                _message.value = "Settings saved"
            } else {
                // Firestore failed: fallback to saving local and inform user
                repo.saveToLocal(updated)
                _uiState.value = updated
                _message.value = "Saved locally (cloud unavailable)"
            }
            _loading.value = false
        }
    }

    fun exportSettings(onResult: (success: Boolean, payload: String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            val json = repo.exportToJsonString()
            _loading.value = false
            onResult(true, json)
        }
    }

    fun importSettings(json: String, onResult: (success: Boolean, msg: String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            val (ok, msg) = repo.importFromJsonString(json)
            if (ok) {
                // refresh uiState from local cache
                val local = repo.loadFromLocal()
                _uiState.value = local
            }
            _loading.value = false
            onResult(ok, msg)
        }
    }

    fun clearLocalCache() {
        viewModelScope.launch {
            repo.clearLocal()
            _uiState.value = SettingsModel()
        }
    }

    fun signOut() {
        auth.signOut()
    }

    // optional: consume message
    fun consumeMessage() {
        _message.value = null
    }
}
