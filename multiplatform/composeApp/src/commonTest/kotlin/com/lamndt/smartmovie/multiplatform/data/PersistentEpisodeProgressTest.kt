package com.lamndt.smartmovie.multiplatform.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PersistentEpisodeProgressTest {
    @Test
    fun progressUsesCanonicalKeysAndRestores() {
        val store = MemoryStore()
        val progress = PersistentEpisodeProgress(store)
        progress.setWatched("1399:1:2", true)
        progress.setWatched("1399:1:3", true)
        progress.setWatched("1399:1:2", false)

        assertEquals(setOf("1399:1:3"), progress.watched.value)
        assertEquals(setOf("1399:1:3"), PersistentEpisodeProgress(store).watched.value)
    }

    @Test
    fun seasonMutationOnlyTouchesListedEpisodes() {
        val progress = PersistentEpisodeProgress(MemoryStore())
        progress.setWatched("1399:1:9", true)
        progress.setSeason(1399, 1, listOf(1, 2, 3), true)
        progress.setSeason(1399, 1, listOf(1, 2), false)
        assertTrue(progress.watched.value.containsAll(setOf("1399:1:3", "1399:1:9")))
    }
}
