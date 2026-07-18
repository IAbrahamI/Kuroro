package ch.privat_network.manga_app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.privat_network.manga_app.data.MangaRepository
import ch.privat_network.manga_app.domain.Manga
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MangaUiState {
    object Loading : MangaUiState()
    data class Success(val mangaList: List<Manga>) : MangaUiState()
    data class Error(val message: String) : MangaUiState()
}

class MangaViewModel : ViewModel() {
    private val repository = MangaRepository()

    private val _uiState = MutableStateFlow<MangaUiState>(MangaUiState.Loading)
    val uiState: StateFlow<MangaUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Dialog State
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _mangaNameInput = MutableStateFlow("")
    val mangaNameInput: StateFlow<String> = _mangaNameInput.asStateFlow()

    private val _isAddingManga = MutableStateFlow(false)
    val isAddingManga: StateFlow<Boolean> = _isAddingManga.asStateFlow()

    private val _isManualFetching = MutableStateFlow(false)
    val isManualFetching: StateFlow<Boolean> = _isManualFetching.asStateFlow()

    init {
        fetchManga()
    }

    fun onOpenAddDialog() {
        _mangaNameInput.value = ""
        _showAddDialog.value = true
    }

    fun onCloseAddDialog() {
        _showAddDialog.value = false
    }

    fun onMangaNameChange(newName: String) {
        _mangaNameInput.value = newName
    }

    fun addManga() {
        val name = _mangaNameInput.value.trim()
        if (name.isEmpty()) return

        viewModelScope.launch {
            _isAddingManga.value = true
            val result = repository.addManga(name)
            
            if (result.isSuccess) {
                onCloseAddDialog()
                refreshListSilently() // Wait for the actual data to be fetched
            }
            _isAddingManga.value = false
        }
    }

    fun manualFetch() {
        viewModelScope.launch {
            _isManualFetching.value = true
            val result = repository.fetchAllUpdates()
            if (result.isSuccess) {
                refreshListSilently() // Wait for the actual data to be fetched
            }
            _isManualFetching.value = false
        }
    }

    fun deleteManga(manga: Manga) {
        viewModelScope.launch {
            val result = repository.deleteManga(manga.title)
            if (result.isSuccess) {
                refreshListSilently()
            }
        }
    }

    /**
     * Re-fetches the manga list from the repository and updates the UI state.
     * Can be called as a standard fetch (with loading screen) or a silent refresh.
     */
    fun fetchManga(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _uiState.value = MangaUiState.Loading
            }
            performFetch()
            _isRefreshing.value = false
        }
    }

    private suspend fun refreshListSilently() {
        performFetch()
    }

    private suspend fun performFetch() {
        try {
            val list = repository.getMangaList()
            if (list.isNotEmpty()) {
                _uiState.value = MangaUiState.Success(list)
            } else {
                // If the list is empty, only show error if we aren't already showing a success list
                if (_uiState.value !is MangaUiState.Success) {
                    _uiState.value = MangaUiState.Error("No manga found or API connection failed.")
                }
            }
        } catch (e: Exception) {
            if (_uiState.value !is MangaUiState.Success) {
                _uiState.value = MangaUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}