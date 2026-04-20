package com.kanime.app.data

object FakeAnimeRepository {
    val hero = Anime(
        id = 1,
        title = "Astral Ronin",
        category = "Action Sci-Fi",
        year = 2026,
        rating = 9.1,
        progress = 0.35f,
        episodes = 12,
        currentEpisode = 5,
        duration = "24 min",
        accent = 0xFF355CFF,
        studio = "Nova Frame",
        audio = "JP / ID Subtitle",
        nextEpisodeText = "Episode 06 tayang Sabtu, 20:30",
        synopsis = "Seorang ronin antar-galaksi memburu relik kuno yang bisa menulis ulang memori seluruh koloni."
    )

    val trending = listOf(
        hero,
        Anime(
            id = 2,
            title = "Moonlit Requiem",
            category = "Fantasy Drama",
            year = 2025,
            rating = 8.7,
            progress = 0.62f,
            episodes = 10,
            currentEpisode = 7,
            duration = "23 min",
            accent = 0xFF9C4DFF,
            studio = "Lunaris Works",
            audio = "JP / EN Subtitle",
            nextEpisodeText = "Episode 08 tayang Jumat, 21:00",
            synopsis = "Putri kerajaan malam menukar suaranya demi menyelamatkan kota terapung dari kutukan."
        ),
        Anime(
            id = 3,
            title = "Circuit Bloom",
            category = "Cyber Slice of Life",
            year = 2026,
            rating = 8.9,
            progress = 0.18f,
            episodes = 8,
            currentEpisode = 2,
            duration = "22 min",
            accent = 0xFF00A88F,
            studio = "Wire Garden",
            audio = "JP / ID Subtitle",
            nextEpisodeText = "Episode 03 tayang Kamis, 19:30",
            synopsis = "Sekelompok siswa teknisi membangun taman digital yang diam-diam terhubung ke masa depan."
        )
    )

    val continueWatching = listOf(
        Anime(
            id = 4,
            title = "Kaiju Harbor",
            category = "Adventure",
            year = 2024,
            rating = 8.4,
            progress = 0.82f,
            episodes = 16,
            currentEpisode = 14,
            duration = "24 min",
            accent = 0xFFFF7A59,
            studio = "Harbor Unit",
            audio = "JP / ID Subtitle",
            nextEpisodeText = "Episode 15 tayang Minggu, 18:00",
            synopsis = "Pelabuhan raksasa menjadi garis depan saat monster laut mulai bermigrasi ke daratan."
        ),
        Anime(
            id = 5,
            title = "Velvet Orbit",
            category = "Romance",
            year = 2025,
            rating = 8.1,
            progress = 0.47f,
            episodes = 11,
            currentEpisode = 5,
            duration = "25 min",
            accent = 0xFFE84C88,
            studio = "Rose Circuit",
            audio = "JP / EN Subtitle",
            nextEpisodeText = "Episode 06 tayang Rabu, 20:00",
            synopsis = "Dua pilot saingan dipaksa berbagi satu kokpit dalam misi diplomatik lintas planet."
        )
    )

    val latestReleases = listOf(
        Anime(
            id = 6,
            title = "Paper Shrine",
            category = "Mystery",
            year = 2026,
            rating = 8.8,
            progress = 0f,
            episodes = 1,
            currentEpisode = 1,
            duration = "48 min",
            accent = 0xFFFFB400,
            studio = "Mori Atelier",
            audio = "JP / ID Subtitle",
            nextEpisodeText = "Episode 02 tayang Senin, 22:00",
            synopsis = "Ritual kertas kuno membuka portal ke distrik yang hilang dari peta kota modern."
        ),
        Anime(
            id = 7,
            title = "Blue Echo Unit",
            category = "Mecha",
            year = 2026,
            rating = 9.0,
            progress = 0f,
            episodes = 2,
            currentEpisode = 2,
            duration = "24 min",
            accent = 0xFF00A6FB,
            studio = "Vector Forge",
            audio = "JP / EN Subtitle",
            nextEpisodeText = "Episode 03 tayang Selasa, 20:30",
            synopsis = "Skuadron baru diluncurkan untuk menghentikan invasi senyap di orbit bumi."
        ),
        Anime(
            id = 8,
            title = "Lanterns of Kawa",
            category = "Supernatural",
            year = 2026,
            rating = 8.6,
            progress = 0f,
            episodes = 3,
            currentEpisode = 3,
            duration = "24 min",
            accent = 0xFF4CAF50,
            studio = "Kawa Pictures",
            audio = "JP / ID Subtitle",
            nextEpisodeText = "Episode 04 tayang Jumat, 18:30",
            synopsis = "Setiap lentera yang terapung di sungai membawa ingatan yang belum sempat diucapkan."
        )
    )

    val upcomingSchedule = listOf(
        hero,
        trending[1],
        latestReleases[1],
        continueWatching[0]
    )

    val editorialPicks = listOf(
        latestReleases[0],
        trending[2],
        continueWatching[1]
    )

    val allAnime = buildList {
        addAll(trending)
        addAll(continueWatching)
        addAll(latestReleases)
    }.distinctBy { it.id }

    val featuredGenres = listOf(
        "All",
        "Action Sci-Fi",
        "Fantasy Drama",
        "Cyber Slice of Life",
        "Adventure",
        "Romance",
        "Mystery",
        "Mecha",
        "Supernatural"
    )
}
