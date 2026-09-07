package com.lamndt.smartmovie.multiplatform.data

import com.lamndt.smartmovie.multiplatform.model.TitleSummary
import com.lamndt.smartmovie.multiplatform.platform.KeyValueStore
import com.lamndt.smartmovie.multiplatform.platform.systemTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

enum class LibraryCollection {
    FAVORITES, WATCHLIST;
    val wireValue: String get() = if (this == FAVORITES) "favorites" else "watchlist"
}

@Serializable
data class LibraryRecord(
    val title: TitleSummary,
    val isFavorite: Boolean = false,
    val isWatchlisted: Boolean = false,
    val favoritedAt: Long? = null,
    val watchlistedAt: Long? = null,
    val updatedAt: Long,
    val syncOrigin: String = "local",
    val favoritePending: Boolean = false,
    val watchlistPending: Boolean = false,
    val remoteRevision: String? = null,
    val accountId: Int? = null,
)

@Serializable
data class LibraryMutation(
    val id: String,
    val libraryKey: String,
    val mediaType: String,
    val mediaId: Int,
    val collection: String,
    val enabled: Boolean,
    val accountId: Int,
    val createdAt: Long,
    val attemptCount: Int = 0,
    val lastError: String? = null,
)

class PersistentLibrary(
    private val store: KeyValueStore,
    private val clock: () -> Long = ::systemTimeMillis,
    private val json: Json = Json { ignoreUnknownKeys = true; explicitNulls = false },
) {
    private val mutableRecords = MutableStateFlow(readRecords())
    private val mutableOutbox = MutableStateFlow(readOutbox())
    private var activeAccountId: Int? = null
    val records: StateFlow<List<LibraryRecord>> = mutableRecords.asStateFlow()

    fun toggle(title: TitleSummary, collection: LibraryCollection) {
        val now = clock()
        val current = mutableRecords.value.associateBy { it.title.libraryKey }.toMutableMap()
        val existing = current[title.libraryKey] ?: LibraryRecord(title = title, updatedAt = now)
        val changed = when (collection) {
            LibraryCollection.FAVORITES -> existing.copy(
                title = title,
                isFavorite = !existing.isFavorite,
                favoritedAt = if (existing.isFavorite) null else now,
                updatedAt = now,
            )
            LibraryCollection.WATCHLIST -> existing.copy(
                title = title,
                isWatchlisted = !existing.isWatchlisted,
                watchlistedAt = if (existing.isWatchlisted) null else now,
                updatedAt = now,
            )
        }
        val synchronized = activeAccountId?.let { accountId ->
            val enabled = if (collection == LibraryCollection.FAVORITES) changed.isFavorite else changed.isWatchlisted
            val mutation = LibraryMutation(
                id = createInstallationId(),
                libraryKey = title.libraryKey,
                mediaType = title.mediaType.wireValue,
                mediaId = title.id,
                collection = collection.wireValue,
                enabled = enabled,
                accountId = accountId,
                createdAt = now,
            )
            mutableOutbox.value = mutableOutbox.value + mutation
            persistOutbox()
            changed.copy(
                syncOrigin = "merged",
                favoritePending = changed.favoritePending || collection == LibraryCollection.FAVORITES,
                watchlistPending = changed.watchlistPending || collection == LibraryCollection.WATCHLIST,
                accountId = accountId,
            )
        } ?: changed
        if (synchronized.isFavorite || synchronized.isWatchlisted) current[title.libraryKey] = synchronized else current.remove(title.libraryKey)
        val next = current.values.sortedByDescending { it.updatedAt }
        mutableRecords.value = next
        store.putString(STORE_KEY, json.encodeToString(next))
    }

    fun membership(libraryKey: String): Pair<Boolean, Boolean> {
        val record = mutableRecords.value.firstOrNull { it.title.libraryKey == libraryKey }
        return (record?.isFavorite == true) to (record?.isWatchlisted == true)
    }

    fun activateAccount(accountId: Int) {
        activeAccountId = accountId
        updateRecords { records -> records.map { if (it.accountId == null) it.copy(accountId = accountId) else it } }
    }

    fun deactivateAccount(removeAccountData: Boolean) {
        val accountId = activeAccountId
        activeAccountId = null
        updateRecords { records ->
            if (removeAccountData && accountId != null) records.filterNot { it.accountId == accountId }
            else records.map {
                it.copy(accountId = null, syncOrigin = "local", favoritePending = false, watchlistPending = false)
            }
        }
        mutableOutbox.value = if (removeAccountData && accountId != null) mutableOutbox.value.filterNot { it.accountId == accountId } else emptyList()
        persistOutbox()
    }

    fun mergeRemote(
        remote: List<TitleSummary>,
        collection: LibraryCollection,
        mediaType: com.lamndt.smartmovie.multiplatform.model.MediaType,
        accountId: Int,
    ) {
        activeAccountId = accountId
        val now = clock()
        val remoteKeys = remote.mapTo(mutableSetOf(), TitleSummary::libraryKey)
        val current = mutableRecords.value.associateBy { it.title.libraryKey }.toMutableMap()
        remote.forEach { title ->
            val existing = current[title.libraryKey] ?: LibraryRecord(title, updatedAt = now)
            current[title.libraryKey] = existing.copy(
                title = title,
                isFavorite = if (collection == LibraryCollection.FAVORITES && !existing.favoritePending) true else existing.isFavorite,
                isWatchlisted = if (collection == LibraryCollection.WATCHLIST && !existing.watchlistPending) true else existing.isWatchlisted,
                syncOrigin = if (existing.syncOrigin == "local") "merged" else "tmdb",
                accountId = accountId,
            )
        }
        current.values.filter { it.title.mediaType == mediaType }.forEach { record ->
            val enabled = if (collection == LibraryCollection.FAVORITES) record.isFavorite else record.isWatchlisted
            val pending = if (collection == LibraryCollection.FAVORITES) record.favoritePending else record.watchlistPending
            if (enabled && record.title.libraryKey !in remoteKeys && !pending) {
                mutableOutbox.value = mutableOutbox.value + LibraryMutation(
                    createInstallationId(), record.title.libraryKey, record.title.mediaType.wireValue, record.title.id,
                    collection.wireValue, true, accountId, now,
                )
                current[record.title.libraryKey] = if (collection == LibraryCollection.FAVORITES) record.copy(favoritePending = true)
                else record.copy(watchlistPending = true)
            } else if (!pending && record.title.libraryKey !in remoteKeys) {
                current[record.title.libraryKey] = if (collection == LibraryCollection.FAVORITES) record.copy(isFavorite = false)
                else record.copy(isWatchlisted = false)
            }
        }
        mutableRecords.value = current.values.sortedByDescending(LibraryRecord::updatedAt)
        persistRecords()
        persistOutbox()
    }

    fun pendingMutations(limit: Int = 100): List<LibraryMutation> = mutableOutbox.value.sortedBy { it.createdAt }.take(limit)

    fun confirmMutation(id: String) {
        val mutation = mutableOutbox.value.firstOrNull { it.id == id } ?: return
        mutableOutbox.value = mutableOutbox.value.filterNot { it.id == id }
        val remaining = mutableOutbox.value.filter { it.libraryKey == mutation.libraryKey }
        updateRecords { records -> records.map { record ->
            if (record.title.libraryKey != mutation.libraryKey) record else record.copy(
                favoritePending = record.favoritePending && remaining.any { it.collection == "favorites" },
                watchlistPending = record.watchlistPending && remaining.any { it.collection == "watchlist" },
                remoteRevision = id,
                syncOrigin = "tmdb",
            )
        } }
        persistOutbox()
    }

    fun failMutation(id: String, message: String) {
        mutableOutbox.value = mutableOutbox.value.map {
            if (it.id == id) it.copy(attemptCount = it.attemptCount + 1, lastError = message.take(500)) else it
        }
        persistOutbox()
    }

    private fun readRecords(): List<LibraryRecord> = store.getString(STORE_KEY)
        ?.let { runCatching { json.decodeFromString<List<LibraryRecord>>(it) }.getOrNull() }
        .orEmpty()

    private fun readOutbox(): List<LibraryMutation> = store.getString(OUTBOX_KEY)
        ?.let { runCatching { json.decodeFromString<List<LibraryMutation>>(it) }.getOrNull() }
        .orEmpty()

    private fun updateRecords(transform: (List<LibraryRecord>) -> List<LibraryRecord>) {
        mutableRecords.value = transform(mutableRecords.value)
        persistRecords()
    }

    private fun persistRecords() { store.putString(STORE_KEY, json.encodeToString(mutableRecords.value)) }
    private fun persistOutbox() { store.putString(OUTBOX_KEY, json.encodeToString(mutableOutbox.value)) }

    companion object {
        private const val STORE_KEY = "smartmovie_library_v2"
        private const val OUTBOX_KEY = "smartmovie_library_outbox_v1"
    }
}

class PersistentEpisodeProgress(private val store: KeyValueStore) {
    private val mutableWatched = MutableStateFlow(read())
    val watched: StateFlow<Set<String>> = mutableWatched.asStateFlow()

    fun setWatched(key: String, watched: Boolean) {
        val next = mutableWatched.value.toMutableSet().apply { if (watched) add(key) else remove(key) }.toSet()
        mutableWatched.value = next
        store.putString(STORE_KEY, next.sorted().joinToString("\n"))
    }

    fun setSeason(seriesId: Int, season: Int, episodes: Collection<Int>, watched: Boolean) {
        episodes.forEach { setWatched("$seriesId:$season:$it", watched) }
    }

    private fun read(): Set<String> = store.getString(STORE_KEY).orEmpty().lineSequence()
        .map(String::trim).filter { it.matches(Regex("\\d+:\\d+:\\d+")) }.toSet()

    private companion object { const val STORE_KEY = "smartmovie.episode_progress.v1" }
}
