package com.kanime.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kanime.app.data.Anime
import com.kanime.app.data.FakeAnimeRepository
import com.kanime.app.ui.theme.KanimeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanimeApp(viewModel: KanimeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedAnime = uiState.selectedAnime
    val playerAnime = uiState.playerAnime
    val hasEnteredApp = uiState.hasEnteredApp

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (hasEnteredApp && (selectedAnime != null || playerAnime != null)) {
                TopAppBar(
                    title = { Text(if (playerAnime != null) "Now playing" else "Detail anime") },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (playerAnime != null) viewModel.closePlayer() else viewModel.closeDetail()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (hasEnteredApp && selectedAnime == null && playerAnime == null) {
                KanimeBottomBar(
                    currentTab = uiState.currentTab,
                    onSelect = viewModel::selectTab
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!hasEnteredApp) {
                when (uiState.entryMode) {
                    EntryMode.WELCOME -> WelcomeScreen(
                        onGetStarted = viewModel::showSignIn,
                        onContinueAsGuest = viewModel::continueAsGuest
                    )
                    EntryMode.SIGN_IN -> SignInScreen(
                        onBack = viewModel::backToWelcome,
                        onSubmit = viewModel::submitSignIn,
                        onContinueAsGuest = viewModel::continueAsGuest
                    )
                    EntryMode.DONE -> Unit
                }
            } else if (playerAnime != null) {
                PlayerScreen(
                    anime = playerAnime,
                    onOpenDetail = viewModel::openDetail
                )
            } else if (selectedAnime != null) {
                AnimeDetailScreen(
                    anime = selectedAnime,
                    isInWatchlist = selectedAnime.id in uiState.watchlistIds,
                    onToggleWatchlist = { viewModel.toggleWatchlist(selectedAnime.id) },
                    onPlay = { viewModel.openPlayer(selectedAnime) }
                )
            } else {
                when (uiState.currentTab) {
                    KanimeTab.HOME -> HomeScreen(
                        uiState = uiState,
                        displayName = uiState.displayName,
                        onOpenDetail = viewModel::openDetail,
                        onPlay = viewModel::openPlayer
                    )
                    KanimeTab.SEARCH -> SearchScreen(
                        uiState = uiState,
                        onSearchChange = viewModel::updateSearchQuery,
                        onGenreSelect = viewModel::updateGenre,
                        onOpenDetail = viewModel::openDetail,
                        onToggleWatchlist = viewModel::toggleWatchlist
                    )
                    KanimeTab.WATCHLIST -> WatchlistScreen(
                        items = uiState.watchlist,
                        onOpenDetail = viewModel::openDetail,
                        onToggleWatchlist = viewModel::toggleWatchlist
                    )
                    KanimeTab.DOWNLOADS -> DownloadsScreen(
                        items = uiState.downloads,
                        onOpenDetail = viewModel::openDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun KanimeBottomBar(currentTab: KanimeTab, onSelect: (KanimeTab) -> Unit) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    ) {
        val items = listOf(
            Triple("Home", Icons.Rounded.Home, KanimeTab.HOME),
            Triple("Explore", Icons.Rounded.Search, KanimeTab.SEARCH),
            Triple("Watchlist", Icons.Rounded.VideoLibrary, KanimeTab.WATCHLIST),
            Triple("Download", Icons.Rounded.Download, KanimeTab.DOWNLOADS)
        )

        items.forEach { (label, icon, tab) ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    uiState: KanimeUiState,
    displayName: String,
    onOpenDetail: (Anime) -> Unit,
    onPlay: (Anime) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            HomeGreeting(displayName = displayName)
        }

        item {
            HeroSection(
                anime = FakeAnimeRepository.hero,
                onOpenDetail = onOpenDetail,
                onPlay = onPlay
            )
        }

        item {
            ContinueWatchingSection(
                items = uiState.continueWatching,
                onOpenDetail = onOpenDetail,
                onPlay = onPlay
            )
        }

        item {
            QuickStatusStrip(uiState = uiState)
        }

        item {
            AnimeCarouselSection(
                title = "Trending minggu ini",
                subtitle = "Pilihan paling ramai di Kanime",
                items = uiState.trending,
                onOpenDetail = onOpenDetail
            )
        }

        item {
            AnimeCarouselSection(
                title = "Rilis terbaru",
                subtitle = "Episode baru siap diputar",
                items = uiState.latestReleases,
                onOpenDetail = onOpenDetail
            )
        }

        item {
            ScheduleSection(
                items = uiState.upcomingSchedule,
                onOpenDetail = onOpenDetail
            )
        }

        item {
            AnimeCarouselSection(
                title = "Pilihan editor",
                subtitle = "Kurasi tontonan buat malam ini",
                items = uiState.editorialPicks,
                onOpenDetail = onOpenDetail
            )
        }
    }
}

@Composable
private fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF355CFF),
                        Color(0xFFF5F7FC)
                    )
                )
            ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "K",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Text(
                    text = "Kanime",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Streaming anime dummy dengan UI modern Material 3 untuk demo Android Kotlin.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.86f)
                )
            }
        }

        item {
            FeatureIntroCard(
                title = "Masuk lebih rapi",
                description = "Mulai dari welcome screen dulu sebelum masuk ke katalog anime."
            )
        }

        item {
            FeatureIntroCard(
                title = "Player dummy siap demo",
                description = "Flow dari home, detail, sampai player sudah tersambung tanpa backend."
            )
        }

        item {
            FeatureIntroCard(
                title = "Explore, watchlist, download",
                description = "Semua state masih dummy, tapi interaksi utama sudah terasa seperti aplikasi streaming."
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KanimePrimaryButton(label = "Get started", onClick = onGetStarted)
                TextButton(onClick = onContinueAsGuest) {
                    Text("Continue as guest")
                }
            }
        }
    }
}

@Composable
private fun SignInScreen(
    onBack: () -> Unit,
    onSubmit: (String) -> Unit,
    onContinueAsGuest: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Masuk ke Kanime",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Tidak ada backend. Nama ini hanya dipakai sebagai identitas dummy di dalam aplikasi.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Nama pengguna") },
                        shape = RoundedCornerShape(24.dp)
                    )
                    KanimePrimaryButton(
                        label = "Masuk",
                        onClick = { onSubmit(name) }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onBack) {
                            Text("Back")
                        }
                        TextButton(onClick = onContinueAsGuest) {
                            Text("Guest")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeGreeting(displayName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Halo, $displayName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Siap lanjut nonton malam ini?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun FeatureIntroCard(title: String, description: String) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.92f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchScreen(
    uiState: KanimeUiState,
    onSearchChange: (String) -> Unit,
    onGenreSelect: (String) -> Unit,
    onOpenDetail: (Anime) -> Unit,
    onToggleWatchlist: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Explore anime",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Cari judul dan filter genre dari katalog dummy Kanime.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Cari anime") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null
                    )
                },
                shape = RoundedCornerShape(24.dp)
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.featuredGenres) { genre ->
                    FilterChip(
                        selected = uiState.selectedGenre == genre,
                        onClick = { onGenreSelect(genre) },
                        label = { Text(genre) }
                    )
                }
            }
        }

        item {
            Text(
                text = "${uiState.filteredAnime.size} anime ditemukan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(uiState.filteredAnime) { anime ->
            DiscoverAnimeCard(
                anime = anime,
                isInWatchlist = anime.id in uiState.watchlistIds,
                onOpenDetail = onOpenDetail,
                onToggleWatchlist = onToggleWatchlist
            )
        }
    }
}

@Composable
private fun WatchlistScreen(
    items: List<Anime>,
    onOpenDetail: (Anime) -> Unit,
    onToggleWatchlist: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                title = "Watchlist",
                subtitle = "Koleksi anime yang sudah kamu simpan"
            )
        }

        if (items.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Watchlist masih kosong",
                    description = "Tambahkan anime dari tab Explore untuk mengisi daftar tontonanmu."
                )
            }
        } else {
            items(items) { anime ->
                DiscoverAnimeCard(
                    anime = anime,
                    isInWatchlist = true,
                    onOpenDetail = onOpenDetail,
                    onToggleWatchlist = onToggleWatchlist
                )
            }
        }
    }
}

@Composable
private fun DownloadsScreen(items: List<Anime>, onOpenDetail: (Anime) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                title = "Downloads",
                subtitle = "Konten offline dummy untuk perjalananmu"
            )
        }

        if (items.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Belum ada download",
                    description = "Nanti di versi backend, episode yang disimpan akan muncul di sini."
                )
            }
        } else {
            items(items) { anime ->
                DownloadItemCard(anime = anime, onOpenDetail = onOpenDetail)
            }
        }
    }
}

@Composable
private fun AnimeDetailScreen(
    anime: Anime,
    isInWatchlist: Boolean,
    onToggleWatchlist: () -> Unit,
    onPlay: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(anime.accent),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.BottomStart),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GenreChip(label = anime.category, inverted = true)
                    Text(
                        text = anime.title,
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${anime.year} • ${anime.episodes} episode • ${anime.duration}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KanimePrimaryButton(label = "Play now", onClick = onPlay)
                    Surface(
                        modifier = Modifier.clickable(onClick = onToggleWatchlist),
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isInWatchlist) {
                                    Icons.Rounded.Bookmark
                                } else {
                                    Icons.Rounded.BookmarkBorder
                                },
                                contentDescription = null
                            )
                            Text(if (isInWatchlist) "Saved" else "Save")
                        }
                    }
                }

                DetailStatRow(anime = anime)

                InfoPanel(
                    title = "Streaming info",
                    body = "${anime.studio} • ${anime.audio}\n${anime.nextEpisodeText}"
                )

                Text(
                    text = anime.synopsis,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Episode preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Episode ${anime.currentEpisode.toString().padStart(2, '0')} • Ready to continue",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        LinearProgressIndicator(
                            progress = { anime.progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(anime.accent)
                        )
                        Text(
                            text = "Progress ${(anime.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSection(
    anime: Anime,
    onOpenDetail: (Anime) -> Unit,
    onPlay: (Anime) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(anime.accent),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "K",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Column {
                    Text(
                        text = "kanime",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Streaming anime dummy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            GenreChip(anime.category, inverted = true)
            Text(
                text = anime.title,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = anime.synopsis,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.82f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KanimePrimaryButton(label = "Watch now", dark = true, onClick = { onPlay(anime) })
                TextButton(onClick = { onOpenDetail(anime) }) {
                    Text("Detail", color = Color.White)
                }
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(190.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Lanjut episode",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Ep ${anime.currentEpisode.toString().padStart(2, '0')} • Shadow Port",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { anime.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(anime.accent),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${(anime.progress * 100).toInt()}% selesai",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ContinueWatchingSection(
    items: List<Anime>,
    onOpenDetail: (Anime) -> Unit,
    onPlay: (Anime) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionHeader(
            title = "Lanjut nonton",
            subtitle = "Cepat kembali ke episode terakhir"
        )
        items.forEach { anime ->
            ContinueWatchingCard(anime = anime, onOpenDetail = onOpenDetail, onPlay = onPlay)
        }
    }
}

@Composable
private fun DiscoverAnimeCard(
    anime: Anime,
    isInWatchlist: Boolean,
    onOpenDetail: (Anime) -> Unit,
    onToggleWatchlist: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenDetail(anime) }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 92.dp, height = 120.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(anime.accent),
                                    Color(anime.accent).copy(alpha = 0.4f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = anime.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${anime.category} • ${anime.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = anime.synopsis,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⭐ ${anime.rating} • ${anime.episodes} eps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { onToggleWatchlist(anime.id) }) {
                    Text(if (isInWatchlist) "Remove" else "Save")
                }
            }
        }
    }
}

@Composable
private fun DownloadItemCard(anime: Anime, onOpenDetail: (Anime) -> Unit) {
    Card(
        modifier = Modifier.clickable { onOpenDetail(anime) },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(anime.accent).copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp),
                    tint = Color(anime.accent)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tersimpan offline • ${anime.duration} • ${anime.episodes} episode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DetailStatRow(anime: Anime) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DetailStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Schedule,
            title = "Durasi",
            value = anime.duration
        )
        DetailStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.VideoLibrary,
            title = "Episode",
            value = anime.episodes.toString()
        )
        DetailStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Bookmark,
            title = "Rating",
            value = anime.rating.toString()
        )
    }
}

@Composable
private fun InfoPanel(title: String, body: String) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailStatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyStateCard(title: String, description: String) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlayerScreen(anime: Anime, onOpenDetail: (Anime) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(anime.accent),
                                Color.Black.copy(alpha = 0.86f)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.BottomStart),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = anime.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Episode ${anime.currentEpisode.toString().padStart(2, '0')} • ${anime.duration}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Dummy player",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { anime.progress.coerceAtLeast(0.08f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(anime.accent)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous")
                        }
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                            Icon(
                                imageVector = Icons.Rounded.Pause,
                                contentDescription = "Pause",
                                modifier = Modifier.padding(18.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Rounded.SkipNext, contentDescription = "Next")
                        }
                    }
                    Text(
                        text = "Streaming info: ${anime.audio} • ${anime.studio}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { onOpenDetail(anime) }) {
                        Text("Lihat detail anime")
                    }
                }
            }
        }
        item {
            InfoPanel(
                title = "Next up",
                body = anime.nextEpisodeText
            )
        }
    }
}

@Composable
private fun ContinueWatchingCard(
    anime: Anime,
    onOpenDetail: (Anime) -> Unit,
    onPlay: (Anime) -> Unit
) {
    Card(
        onClick = { onOpenDetail(anime) },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 88.dp, height = 112.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(anime.accent),
                                Color(anime.accent).copy(alpha = 0.45f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { onPlay(anime) }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${anime.category} • Ep ${anime.currentEpisode}/${anime.episodes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { anime.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(anime.accent)
                )
                Text(
                    text = "Progress ${(anime.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickStatusStrip(uiState: KanimeUiState) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusChip(
            label = "Trending",
            value = uiState.trending.size.toString(),
            icon = Icons.Rounded.Subscriptions
        )
        StatusChip(
            label = "Watchlist",
            value = uiState.watchlist.size.toString(),
            icon = Icons.Rounded.Bookmark
        )
        StatusChip(
            label = "Offline",
            value = uiState.downloads.size.toString(),
            icon = Icons.Rounded.Download
        )
    }
}

@Composable
private fun StatusChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ScheduleSection(items: List<Anime>, onOpenDetail: (Anime) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionHeader(
            title = "Jadwal update",
            subtitle = "Pantau episode berikutnya"
        )
        items.forEach { anime ->
            Card(
                modifier = Modifier.clickable { onOpenDetail(anime) },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(anime.accent).copy(alpha = 0.16f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            modifier = Modifier.padding(14.dp),
                            tint = Color(anime.accent)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = anime.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = anime.nextEpisodeText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Ep ${anime.currentEpisode + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimeCarouselSection(
    title: String,
    subtitle: String,
    items: List<Anime>,
    onOpenDetail: (Anime) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader(
            title = title,
            subtitle = subtitle,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(items) { anime ->
                AnimePosterCard(anime = anime, onOpenDetail = onOpenDetail)
            }
        }
    }
}

@Composable
private fun AnimePosterCard(anime: Anime, onOpenDetail: (Anime) -> Unit) {
    Card(
        onClick = { onOpenDetail(anime) },
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(anime.accent),
                                Color(anime.accent).copy(alpha = 0.35f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = anime.category,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.82f),
                    modifier = Modifier.align(Alignment.TopStart)
                )
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "${anime.year} • ⭐ ${anime.rating}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = anime.synopsis,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GenreChip(label: String, inverted: Boolean = false) {
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                color = if (inverted) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (inverted) {
                Color.White.copy(alpha = 0.14f)
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    )
}

@Composable
private fun KanimePrimaryButton(
    label: String,
    dark: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (dark) Color.White else MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = if (dark) Color.Black else MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = label,
                color = if (dark) Color.Black else MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun KanimePreview() {
    KanimeTheme {
        KanimeApp()
    }
}
