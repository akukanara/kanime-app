package com.kanime.app.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.kanime.app.data.Anime
import com.kanime.app.data.FakeAnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

enum class KanimeTab {
    HOME,
    SEARCH,
    WATCHLIST,
    DOWNLOADS
}

enum class EntryMode {
    WELCOME,
    SIGN_IN,
    DONE
}

@Immutable
data class KanimeUiState(
    val entryMode: EntryMode = EntryMode.WELCOME,
    val displayName: String = "Guest Otaku",
    val currentTab: KanimeTab = KanimeTab.HOME,
    val selectedAnime: Anime? = null,
    val playerAnime: Anime? = null,
    val searchQuery: String = "",
    val selectedGenre: String = "All",
    val watchlistIds: Set<Int> = setOf(1, 3, 7),
    val downloadedIds: Set<Int> = setOf(4, 7),
    val allAnime: List<Anime> = FakeAnimeRepository.allAnime,
    val trending: List<Anime> = FakeAnimeRepository.trending,
    val continueWatching: List<Anime> = FakeAnimeRepository.continueWatching,
    val latestReleases: List<Anime> = FakeAnimeRepository.latestReleases,
    val upcomingSchedule: List<Anime> = FakeAnimeRepository.upcomingSchedule,
    val editorialPicks: List<Anime> = FakeAnimeRepository.editorialPicks,
    val featuredGenres: List<String> = FakeAnimeRepository.featuredGenres
) {
    val watchlist: List<Anime>
        get() = allAnime.filter { it.id in watchlistIds }

    val downloads: List<Anime>
        get() = allAnime.filter { it.id in downloadedIds }

    val filteredAnime: List<Anime>
        get() = allAnime.filter { anime ->
            val matchesGenre = selectedGenre == "All" || anime.category == selectedGenre
            val matchesQuery = searchQuery.isBlank() || anime.title.contains(searchQuery, ignoreCase = true)
            matchesGenre && matchesQuery
        }

    val hasEnteredApp: Boolean
        get() = entryMode == EntryMode.DONE
}

class KanimeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(KanimeUiState())
    val uiState: StateFlow<KanimeUiState> = _uiState

    fun selectTab(tab: KanimeTab) {
        _uiState.update { state ->
            state.copy(
                currentTab = tab,
                selectedAnime = null,
                playerAnime = null
            )
        }
    }

    fun showSignIn() {
        _uiState.update { it.copy(entryMode = EntryMode.SIGN_IN) }
    }

    fun continueAsGuest() {
        _uiState.update { it.copy(entryMode = EntryMode.DONE, displayName = "Guest Otaku") }
    }

    fun submitSignIn(name: String) {
        val sanitized = name.trim().ifBlank { "Kanime User" }
        _uiState.update { it.copy(entryMode = EntryMode.DONE, displayName = sanitized) }
    }

    fun backToWelcome() {
        _uiState.update { it.copy(entryMode = EntryMode.WELCOME) }
    }

    fun openDetail(anime: Anime) {
        _uiState.update { it.copy(selectedAnime = anime, playerAnime = null) }
    }

    fun closeDetail() {
        _uiState.update { it.copy(selectedAnime = null) }
    }

    fun openPlayer(anime: Anime) {
        _uiState.update { it.copy(playerAnime = anime, selectedAnime = null) }
    }

    fun closePlayer() {
        _uiState.update { it.copy(playerAnime = null) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateGenre(genre: String) {
        _uiState.update { it.copy(selectedGenre = genre) }
    }

    fun toggleWatchlist(animeId: Int) {
        _uiState.update { state ->
            val updated = state.watchlistIds.toMutableSet().apply {
                if (!add(animeId)) remove(animeId)
            }
            state.copy(watchlistIds = updated)
        }
    }
}
