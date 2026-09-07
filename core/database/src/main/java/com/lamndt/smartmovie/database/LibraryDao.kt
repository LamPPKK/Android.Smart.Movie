package com.lamndt.smartmovie.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Query("SELECT * FROM episode_watch_progress WHERE episodeKey = :key LIMIT 1")
    fun observeEpisodeWatch(key: String): Flow<EpisodeWatchEntity?>

    @Query("SELECT episodeNumber FROM episode_watch_progress WHERE seriesId = :seriesId AND seasonNumber = :seasonNumber")
    fun observeWatchedEpisodes(seriesId: Int, seasonNumber: Int): Flow<List<Int>>

    @Upsert
    suspend fun upsertEpisodeWatch(item: EpisodeWatchEntity)

    @Query("DELETE FROM episode_watch_progress WHERE episodeKey = :key")
    suspend fun deleteEpisodeWatch(key: String)

    @Query("DELETE FROM episode_watch_progress WHERE episodeKey IN (:keys)")
    suspend fun deleteEpisodeWatches(keys: List<String>)

    @Query("SELECT * FROM library_items")
    fun observeAll(): Flow<List<LibraryItemEntity>>

    @Query("SELECT * FROM library_items WHERE libraryKey = :key LIMIT 1")
    fun observe(key: String): Flow<LibraryItemEntity?>

    @Query("SELECT * FROM library_items WHERE libraryKey = :key LIMIT 1")
    suspend fun get(key: String): LibraryItemEntity?

    @Upsert
    suspend fun upsert(item: LibraryItemEntity)

    @Query("SELECT * FROM library_items")
    suspend fun getAll(): List<LibraryItemEntity>

    @Query("DELETE FROM library_items WHERE accountId = :accountId")
    suspend fun deleteAccountItems(accountId: Int)

    @Upsert
    suspend fun upsertOutbox(item: LibraryOutboxEntity)

    @Query("SELECT * FROM library_outbox ORDER BY createdAt ASC LIMIT :limit")
    suspend fun pendingOutbox(limit: Int): List<LibraryOutboxEntity>

    @Query("SELECT * FROM library_outbox WHERE mutationId = :id LIMIT 1")
    suspend fun outbox(id: String): LibraryOutboxEntity?

    @Query("SELECT * FROM library_outbox WHERE libraryKey = :key")
    suspend fun outboxForKey(key: String): List<LibraryOutboxEntity>

    @Delete
    suspend fun deleteOutbox(item: LibraryOutboxEntity)

    @Query("DELETE FROM library_outbox WHERE accountId = :accountId")
    suspend fun deleteAccountOutbox(accountId: Int)

    @Query("DELETE FROM library_outbox")
    suspend fun clearOutbox()

    @Upsert
    suspend fun upsertAccountMutation(item: AccountMutationOutboxEntity)

    @Query("SELECT * FROM account_mutation_outbox WHERE accountId = :accountId ORDER BY createdAt ASC, mutationId ASC LIMIT :limit")
    suspend fun pendingAccountMutations(accountId: Int, limit: Int): List<AccountMutationOutboxEntity>

    @Query("SELECT * FROM account_mutation_outbox WHERE mutationId = :id LIMIT 1")
    suspend fun accountMutation(id: String): AccountMutationOutboxEntity?

    @Delete
    suspend fun deleteAccountMutation(item: AccountMutationOutboxEntity)

    @Query("DELETE FROM account_mutation_outbox WHERE accountId = :accountId")
    suspend fun clearAccountMutations(accountId: Int)
}
