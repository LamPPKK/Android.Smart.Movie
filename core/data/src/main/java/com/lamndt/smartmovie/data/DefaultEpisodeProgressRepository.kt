package com.lamndt.smartmovie.data

import androidx.room.withTransaction
import com.lamndt.smartmovie.database.EpisodeWatchEntity
import com.lamndt.smartmovie.database.SmartMovieDatabase
import com.lamndt.smartmovie.model.EpisodeProgressRepository
import com.lamndt.smartmovie.model.EpisodeWatchKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultEpisodeProgressRepository(
    private val database: SmartMovieDatabase,
    private val now: () -> Long = System::currentTimeMillis,
) : EpisodeProgressRepository {
    private val dao = database.libraryDao()

    override fun observeWatched(key: EpisodeWatchKey): Flow<Boolean> =
        dao.observeEpisodeWatch(key.rawValue).map { it != null }

    override fun observeSeason(seriesId: Int, seasonNumber: Int): Flow<Set<Int>> =
        dao.observeWatchedEpisodes(seriesId, seasonNumber).map(List<Int>::toSet)

    override suspend fun setWatched(key: EpisodeWatchKey, watched: Boolean) {
        if (watched) dao.upsertEpisodeWatch(key.toEntity(now())) else dao.deleteEpisodeWatch(key.rawValue)
    }

    override suspend fun setSeasonWatched(
        seriesId: Int,
        seasonNumber: Int,
        episodeNumbers: Collection<Int>,
        watched: Boolean,
    ) {
        val keys = episodeNumbers.distinct().map { EpisodeWatchKey(seriesId, seasonNumber, it) }
        if (keys.isEmpty()) return
        database.withTransaction {
            if (watched) {
                val timestamp = now()
                keys.forEach { dao.upsertEpisodeWatch(it.toEntity(timestamp)) }
            } else {
                dao.deleteEpisodeWatches(keys.map(EpisodeWatchKey::rawValue))
            }
        }
    }

    private fun EpisodeWatchKey.toEntity(timestamp: Long) = EpisodeWatchEntity(
        episodeKey = rawValue,
        seriesId = seriesId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        watchedAt = timestamp,
        updatedAt = timestamp,
    )
}
