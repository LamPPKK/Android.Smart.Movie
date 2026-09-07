package com.lamndt.smartmovie.network

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.lamndt.smartmovie.model.AccountProfile
import com.lamndt.smartmovie.model.AccountRepository
import com.lamndt.smartmovie.model.AuthAttempt
import com.lamndt.smartmovie.model.AuthSession
import com.lamndt.smartmovie.model.LibraryCollection
import com.lamndt.smartmovie.model.MediaType
import com.lamndt.smartmovie.model.MutationResult
import com.lamndt.smartmovie.model.PagedResult
import com.lamndt.smartmovie.model.TitleSummary
import com.lamndt.smartmovie.model.TitleAccountState
import com.lamndt.smartmovie.model.EpisodeAccountState
import com.lamndt.smartmovie.model.UserList
import com.lamndt.smartmovie.model.UserListItemMutation
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AccountNetworkRepository(
    context: Context,
    baseUrl: String,
    json: Json = CatalogNetworkDataSource.defaultJson,
    okHttpClient: OkHttpClient = OkHttpClient.Builder().build(),
    private val clientIdProvider: suspend () -> String = { InstallationIdStore(context.applicationContext).get() },
    private val tokenStore: SessionTokenStore = KeystoreSessionTokenStore(context.applicationContext),
) : AccountRepository {
    private val service = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(CatalogService::class.java)

    override suspend fun createAuthAttempt(returnUri: String, mode: String): AuthAttempt =
        service.createAuthAttempt(clientIdProvider(), AuthAttemptRequest(returnUri, mode))

    override suspend fun authAttempt(id: String, deviceCode: String?): String =
        service.authAttempt(clientIdProvider(), id, deviceCode).status

    override suspend fun completeAuth(id: String, deviceCode: String?): AuthSession =
        service.completeAuth(clientIdProvider(), CompleteAuthRequest(id, deviceCode)).also {
            it.sessionToken?.let(tokenStore::save)
        }

    override suspend fun profile(): AccountProfile = service.profile(clientIdProvider(), authorization())

    override suspend fun accountDetails(accountId: Int): AccountProfile {
        require(accountId > 0) { "accountId must be positive" }
        return service.accountDetails(clientIdProvider(), authorization(), accountId)
    }

    override suspend fun accountState(mediaType: MediaType, mediaId: Int): TitleAccountState =
        service.accountState(clientIdProvider(), authorization(), mediaType.wireValue, mediaId)

    override suspend fun episodeAccountState(seriesId: Int, season: Int, episode: Int): EpisodeAccountState =
        service.episodeAccountState(clientIdProvider(), authorization(), seriesId, season, episode)

    override suspend fun logout() {
        runCatching { service.logout(clientIdProvider(), authorization()) }
        tokenStore.clear()
    }

    override suspend fun library(
        collection: LibraryCollection,
        mediaType: MediaType,
        page: Int,
        language: String,
    ): PagedResult<TitleSummary> = service.accountLibrary(
        clientIdProvider(), authorization(), collection.wireValue, mediaType.wireValue, page, language, "created_at.desc",
    )

    override suspend fun setLibrary(
        collection: LibraryCollection,
        mediaType: MediaType,
        mediaId: Int,
        enabled: Boolean,
        mutationId: String,
    ): MutationResult = service.setLibrary(
        clientIdProvider(), authorization(), collection.wireValue, mediaType.wireValue,
        LibraryMutation(mediaId, enabled, mutationId),
    )

    override suspend fun setRating(mediaType: MediaType, mediaId: Int, value: Double?, mutationId: String): MutationResult =
        if (value == null) service.deleteRating(clientIdProvider(), authorization(), mutationId, mediaType.wireValue, mediaId)
        else service.setRating(clientIdProvider(), authorization(), mediaType.wireValue, mediaId, RatingMutation(value, mutationId))

    override suspend fun setEpisodeRating(
        seriesId: Int,
        season: Int,
        episode: Int,
        value: Double?,
        mutationId: String,
    ): MutationResult = if (value == null) {
        service.deleteEpisodeRating(clientIdProvider(), authorization(), mutationId, seriesId, season, episode)
    } else {
        service.setEpisodeRating(clientIdProvider(), authorization(), seriesId, season, episode, RatingMutation(value, mutationId))
    }

    override suspend fun recommendations(mediaType: MediaType, page: Int, language: String): PagedResult<TitleSummary> =
        service.accountRecommendations(clientIdProvider(), authorization(), mediaType.wireValue, page, language)

    override suspend fun lists(page: Int): PagedResult<UserList> =
        service.lists(clientIdProvider(), authorization(), page)

    override suspend fun list(id: Int, page: Int, language: String): UserList =
        service.list(clientIdProvider(), authorization(), id, page, language)

    override suspend fun createList(
        name: String,
        description: String,
        isPublic: Boolean,
        region: String,
        language: String,
        mutationId: String,
    ): MutationResult = service.createList(
        clientIdProvider(), authorization(), ListMetadata(name, description, isPublic, region, language, mutationId),
    )

    override suspend fun updateList(
        id: Int,
        name: String,
        description: String,
        isPublic: Boolean,
        mutationId: String,
    ): MutationResult = service.updateList(
        clientIdProvider(), authorization(), id, ListMetadata(name, description, isPublic, mutationId = mutationId),
    )

    override suspend fun deleteList(id: Int, mutationId: String): MutationResult =
        service.deleteList(clientIdProvider(), authorization(), mutationId, id)

    override suspend fun mutateListItems(
        id: Int,
        items: List<UserListItemMutation>,
        remove: Boolean,
        mutationId: String,
    ): MutationResult {
        val body = ListItemsMutation(items.map { ListItemPayload(it.mediaType.wireValue, it.mediaId, it.comment) }, mutationId)
        return if (remove) service.removeListItems(clientIdProvider(), authorization(), id, body)
        else service.addListItems(clientIdProvider(), authorization(), id, body)
    }

    private fun authorization(): String = "Bearer ${tokenStore.load() ?: error("A SmartMovie session is required")}"
}

interface SessionTokenStore {
    fun load(): String?
    fun save(token: String)
    fun clear()
}

class KeystoreSessionTokenStore(context: Context) : SessionTokenStore {
    private val preferences = context.getSharedPreferences("smartmovie_secure_session", Context.MODE_PRIVATE)

    override fun load(): String? {
        val payload = preferences.getString(TOKEN, null) ?: return null
        return runCatching {
            val bytes = Base64.decode(payload, Base64.NO_WRAP)
            require(bytes.size > IV_SIZE)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, bytes.copyOfRange(0, IV_SIZE)))
            cipher.doFinal(bytes.copyOfRange(IV_SIZE, bytes.size)).toString(Charsets.UTF_8)
        }.getOrElse {
            clear()
            null
        }
    }

    override fun save(token: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val encrypted = cipher.iv + cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        preferences.edit().putString(TOKEN, Base64.encodeToString(encrypted, Base64.NO_WRAP)).apply()
    }

    override fun clear() { preferences.edit().remove(TOKEN).apply() }

    private fun secretKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val ALIAS = "smartmovie_session_key_v1"
        const val TOKEN = "opaque_session"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE = 12
    }
}

val LibraryCollection.wireValue: String
    get() = if (this == LibraryCollection.FAVORITES) "favorites" else "watchlist"
