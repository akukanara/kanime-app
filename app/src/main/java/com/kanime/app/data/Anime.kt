package com.kanime.app.data

data class Anime(
    val id: Int,
    val title: String,
    val category: String,
    val year: Int,
    val rating: Double,
    val progress: Float,
    val episodes: Int,
    val currentEpisode: Int,
    val duration: String,
    val accent: Long,
    val studio: String,
    val audio: String,
    val nextEpisodeText: String,
    val synopsis: String
)
