package com.lamndt.smartmovie.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.lamndt.smartmovie.database.SmartMovieDatabase
import com.lamndt.smartmovie.model.EpisodeWatchKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DefaultEpisodeProgressRepositoryTest {
    private lateinit var database: SmartMovieDatabase
    private lateinit var repository: DefaultEpisodeProgressRepository

    @Before fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(), SmartMovieDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = DefaultEpisodeProgressRepository(database) { 1234L }
    }

    @After fun tearDown() = database.close()

    @Test fun episodeProgress_isOfflineAndUsesCanonicalKey() = runTest {
        val key = EpisodeWatchKey(1399, 1, 2)
        assertThat(repository.observeWatched(key).first()).isFalse()
        repository.setWatched(key, true)
        assertThat(repository.observeWatched(key).first()).isTrue()
        assertThat(database.libraryDao().observeWatchedEpisodes(1399, 1).first()).containsExactly(2)
        repository.setWatched(key, false)
        assertThat(repository.observeWatched(key).first()).isFalse()
    }

    @Test fun seasonProgress_onlyChangesListedEpisodes() = runTest {
        repository.setWatched(EpisodeWatchKey(1399, 1, 9), true)
        repository.setSeasonWatched(1399, 1, listOf(1, 2, 3), true)
        assertThat(repository.observeSeason(1399, 1).first()).containsExactly(1, 2, 3, 9)
        repository.setSeasonWatched(1399, 1, listOf(1, 2), false)
        assertThat(repository.observeSeason(1399, 1).first()).containsExactly(3, 9)
    }
}
