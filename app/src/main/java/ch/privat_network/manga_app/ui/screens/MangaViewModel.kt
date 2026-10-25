package ch.privat_network.manga_app.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.privat_network.manga_app.data.MangaList
import ch.privat_network.manga_app.data.MangaRepository
import ch.privat_network.manga_app.domain.Manga
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed class MangaUiState {
    object Loading : MangaUiState()
    /**
     * [fetchedAt] is when the list came from the server.
     * [isOffline] means the server couldn't be reached and this is the saved list.
     */
    data class Success(
        val mangaList: List<Manga>,
        val fetchedAt: Long,
        val isOffline: Boolean = false
    ) : MangaUiState()
    data class Error(val message: String) : MangaUiState()
}

class MangaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MangaRepository(application)

    private val _uiState = MutableStateFlow<MangaUiState>(MangaUiState.Loading)
    val uiState: StateFlow<MangaUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // One-off messages shown as a snackbar
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    // Dialog State
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _mangaNameInput = MutableStateFlow("")
    val mangaNameInput: StateFlow<String> = _mangaNameInput.asStateFlow()

    private val _isAddingManga = MutableStateFlow(false)
    val isAddingManga: StateFlow<Boolean> = _isAddingManga.asStateFlow()

    init {
        viewModelScope.launch {
            // Show the saved list right away, then update it from the server
            val cached = repository.getCachedMangaList()
            if (cached != null) {
                _uiState.value = cached.toUiState()
                _isRefreshing.value = true
                performFetch()
                _isRefreshing.value = false
            } else {
                performFetch()
            }
        }
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
            repository.addManga(name)
                .onSuccess {
                    onCloseAddDialog()
                    performFetch() // Wait for the actual data to be fetched
                }
                .onFailure { _messages.send("Couldn't add \"$name\": ${it.describe()}") }
            _isAddingManga.value = false
        }
    }

    fun deleteManga(manga: Manga) {
        viewModelScope.launch {
            repository.deleteManga(manga.title)
                .onSuccess { performFetch() }
                .onFailure { _messages.send("Couldn't delete \"${manga.title}\": ${it.describe()}") }
        }
    }

    /** Plain list load (GET only), with the full-screen loader. Used on start and by Retry. */
    fun loadManga() {
        viewModelScope.launch {
            _uiState.value = MangaUiState.Loading
            performFetch()
        }
    }

    /**
     * Pull-to-refresh: lets the server scrape for new chapters (PUT), then re-reads the list (GET).
     * The current list stays visible meanwhile; the list is re-read even if the scrape failed.
     */
    fun refresh() {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                val update = repository.fetchAllUpdates()
                performFetch()
                // Offline is already shown by the banner, so only report a scrape failure when online
                val state = _uiState.value
                if (update.isFailure && !(state is MangaUiState.Success && state.isOffline)) {
                    _messages.send("Couldn't check for updates: ${update.exceptionOrNull()?.describe()}")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun performFetch() {
        repository.getMangaList()
            .onSuccess { _uiState.value = it.toUiState() }
            .onFailure {
                // Fall back to the list we already show, or the saved one, marked as offline
                val current = _uiState.value as? MangaUiState.Success
                    ?: repository.getCachedMangaList()?.toUiState()
                _uiState.value = current?.copy(isOffline = true)
                    ?: MangaUiState.Error("Couldn't load mangas: ${it.describe()}")
            }
    }

    private fun MangaList.toUiState() = MangaUiState.Success(mangas, fetchedAt)

    private fun Throwable.describe(): String = localizedMessage ?: javaClass.simpleName
}
