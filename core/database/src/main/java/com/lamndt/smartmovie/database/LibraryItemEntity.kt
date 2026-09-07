package com.lamndt.smartmovie.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "library_items")
data class LibraryItemEntity(
    @PrimaryKey val libraryKey: String,
    val tmdbId: Int,
    val mediaType: String,
    val title: String,
    val originalTitle: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val adult: Boolean = false,
    val isFavorite: Boolean,
    val isWatchlisted: Boolean,
    val favoritedAt: Long?,
    val watchlistedAt: Long?,
    val updatedAt: Long,
    val syncOrigin: String = "local",
    val favoritePending: Boolean = false,
    val watchlistPending: Boolean = false,
    val remoteRevision: String? = null,
    val accountId: Int? = null,
)

@Entity(tableName = "library_outbox")
data class LibraryOutboxEntity(
    @PrimaryKey val mutationId: String,
    val libraryKey: String,
    val mediaType: String,
    val mediaId: Int,
    val collection: String,
    val enabled: Boolean,
    val accountId: Int,
    val createdAt: Long,
    val attemptCount: Int = 0,
    val lastAttemptAt: Long? = null,
    val lastError: String? = null,
)

@Entity(tableName = "account_mutation_outbox")
data class AccountMutationOutboxEntity(
    @PrimaryKey val mutationId: String,
    val accountId: Int,
    val payloadJson: String,
    val createdAt: Long,
    val attemptCount: Int = 0,
    val lastAttemptAt: Long? = null,
    val lastError: String? = null,
)

@Entity(
    tableName = "episode_watch_progress",
    indices = [Index(value = ["seriesId", "seasonNumber"])],
)
data class EpisodeWatchEntity(
    @PrimaryKey val episodeKey: String,
    val seriesId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val watchedAt: Long,
    val updatedAt: Long,
)
