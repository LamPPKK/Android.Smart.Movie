package com.lamndt.smartmovie.multiplatform.data

import com.lamndt.smartmovie.multiplatform.model.AccountProfile
import com.lamndt.smartmovie.multiplatform.model.AuthAttempt
import com.lamndt.smartmovie.multiplatform.model.AuthSession
import com.lamndt.smartmovie.multiplatform.model.EpisodeAccountState
import com.lamndt.smartmovie.multiplatform.model.MediaType
import com.lamndt.smartmovie.multiplatform.model.MutationResult
import com.lamndt.smartmovie.multiplatform.model.PagedResult
import com.lamndt.smartmovie.multiplatform.model.TitleSummary
import com.lamndt.smartmovie.multiplatform.model.TitleAccountState
import com.lamndt.smartmovie.multiplatform.model.UserList
import com.lamndt.smartmovie.multiplatform.platform.SessionCredentialStore
import com.lamndt.smartmovie.multiplatform.platform.createSessionCredentialStore
import com.lamndt.smartmovie.multiplatform.platform.applySessionRequestOptions
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface AccountApi {
    suspend fun createAuthAttempt(returnUri: String, mode: String): AuthAttempt
    suspend fun authAttempt(id: String, deviceCode: String?): String
    suspend fun completeAuth(id: String, deviceCode: String?): AuthSession
    suspend fun profile(): AccountProfile
    suspend fun accountDetails(accountId: Int): AccountProfile = profile()
    suspend fun accountState(mediaType: MediaType, id: Int): TitleAccountState
    suspend fun episodeAccountState(seriesId: Int, season: Int, episode: Int): EpisodeAccountState
    suspend fun refreshCSRF(): String
    suspend fun logout()
    suspend fun library(collection: LibraryCollection, mediaType: MediaType, page: Int, language: String): PagedResult<TitleSummary>
    suspend fun setLibrary(
        collection: LibraryCollection,
        mediaType: MediaType,
        mediaId: Int,
        enabled: Boolean,
        mutationId: String,
    ): MutationResult
    suspend fun setRating(mediaType: MediaType, id: Int, value: Double?, mutationId: String): MutationResult
    suspend fun setEpisodeRating(seriesId: Int, season: Int, episode: Int, value: Double?, mutationId: String): MutationResult
    suspend fun recommendations(mediaType: MediaType, page: Int, language: String): PagedResult<TitleSummary>
    suspend fun lists(page: Int): PagedResult<UserList>
    suspend fun list(id: Int, page: Int, language: String): UserList
    suspend fun createList(name: String, description: String, public: Boolean, region: String, language: String, mutationId: String): MutationResult
    suspend fun updateList(id: Int, name: String, description: String, public: Boolean, mutationId: String): MutationResult
    suspend fun deleteList(id: Int, mutationId: String): MutationResult
    suspend fun mutateListItems(id: Int, items: List<ListItemMutation>, remove: Boolean, mutationId: String): MutationResult
}

class KtorAccountApi(
    baseUrl: String,
    private val clientId: String,
    private val credentials: SessionCredentialStore = createSessionCredentialStore(),
    private val format: Json = Json { ignoreUnknownKeys = true; explicitNulls = false },
) : AccountApi {
    private val root = baseUrl.trimEnd('/')
    private val client = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) { json(format) }
    }
    private var csrfToken: String? = null

    override suspend fun createAuthAttempt(returnUri: String, mode: String): AuthAttempt = response {
        client.post("$root/v2/auth/attempts") {
            headers(); contentType(ContentType.Application.Json); setBody(AuthAttemptRequest(returnUri, mode))
        }
    }

    override suspend fun authAttempt(id: String, deviceCode: String?): String = response<AuthAttemptStatus> {
        client.get("$root/v2/auth/attempts/$id") { headers(); deviceCode?.let { parameter("device_code", it) } }
    }.status

    override suspend fun completeAuth(id: String, deviceCode: String?): AuthSession = response<AuthSession> {
        client.post("$root/v2/auth/complete") {
            headers(); contentType(ContentType.Application.Json); setBody(CompleteAuthRequest(id, deviceCode))
        }
    }.also {
        csrfToken = it.csrfToken
        it.sessionToken?.let(credentials::save)
    }

    override suspend fun profile(): AccountProfile {
        val profile = response<AccountProfile> {
            client.get("$root/v2/account/profile") { headers(authenticated = true) }
        }
        if (csrfToken == null) runCatching { refreshCSRF() }
        return profile
    }

    override suspend fun accountDetails(accountId: Int): AccountProfile {
        require(accountId > 0) { "accountId must be positive" }
        return response {
            client.get("$root/v2/account/profile/$accountId") { headers(authenticated = true) }
        }
    }

    override suspend fun accountState(mediaType: MediaType, id: Int): TitleAccountState = response {
        client.get("$root/v2/account/state/${mediaType.wireValue}/$id") { headers(authenticated = true) }
    }

    override suspend fun episodeAccountState(seriesId: Int, season: Int, episode: Int): EpisodeAccountState = response {
        client.get("$root/v2/account/state/episode/$seriesId/$season/$episode") { headers(authenticated = true) }
    }

    override suspend fun refreshCSRF(): String = response<CSRFResponse> {
        client.get("$root/v2/auth/csrf") { headers(authenticated = true) }
    }.csrfToken.also { csrfToken = it }

    override suspend fun logout() {
        runCatching { response<LogoutResponse> { client.post("$root/v2/auth/logout") { headers(authenticated = true) } } }
        credentials.clear()
    }

    override suspend fun library(
        collection: LibraryCollection,
        mediaType: MediaType,
        page: Int,
        language: String,
    ): PagedResult<TitleSummary> = response {
        client.get("$root/v2/account/${collection.wireValue}/${mediaType.wireValue}") {
            headers(authenticated = true); parameter("page", page); parameter("language", language); parameter("sort_by", "created_at.desc")
        }
    }

    override suspend fun setLibrary(
        collection: LibraryCollection,
        mediaType: MediaType,
        mediaId: Int,
        enabled: Boolean,
        mutationId: String,
    ): MutationResult = response {
        client.put("$root/v2/account/${collection.wireValue}/${mediaType.wireValue}") {
            headers(authenticated = true, mutationId = mutationId)
            contentType(ContentType.Application.Json)
            setBody(AccountLibraryMutation(mediaId, enabled, mutationId))
        }
    }

    override suspend fun setRating(mediaType: MediaType, id: Int, value: Double?, mutationId: String): MutationResult = response {
        if (value == null) client.delete("$root/v2/account/ratings/${mediaType.wireValue}/$id") {
            headers(authenticated = true, mutationId = mutationId)
        }
        else client.put("$root/v2/account/ratings/${mediaType.wireValue}/$id") {
            headers(authenticated = true, mutationId = mutationId)
            contentType(ContentType.Application.Json)
            setBody(RatingMutation(value, mutationId))
        }
    }

    override suspend fun setEpisodeRating(
        seriesId: Int,
        season: Int,
        episode: Int,
        value: Double?,
        mutationId: String,
    ): MutationResult = response {
        val path = "$root/v2/account/ratings/episode/$seriesId/$season/$episode"
        if (value == null) client.delete(path) { headers(authenticated = true, mutationId = mutationId) }
        else client.put(path) {
            headers(authenticated = true, mutationId = mutationId)
            contentType(ContentType.Application.Json)
            setBody(RatingMutation(value, mutationId))
        }
    }

    override suspend fun recommendations(mediaType: MediaType, page: Int, language: String): PagedResult<TitleSummary> = response {
        client.get("$root/v2/account/recommendations/${mediaType.wireValue}") {
            headers(authenticated = true); parameter("page", page); parameter("language", language)
        }
    }

    override suspend fun lists(page: Int): PagedResult<UserList> = response {
        client.get("$root/v2/account/lists") { headers(authenticated = true); parameter("page", page) }
    }

    override suspend fun list(id: Int, page: Int, language: String): UserList = response {
        client.get("$root/v2/account/lists/$id") { headers(authenticated = true); parameter("page", page); parameter("language", language) }
    }

    override suspend fun createList(
        name: String,
        description: String,
        public: Boolean,
        region: String,
        language: String,
        mutationId: String,
    ): MutationResult = response {
        client.post("$root/v2/account/lists") {
            headers(authenticated = true, mutationId = mutationId); contentType(ContentType.Application.Json)
            setBody(ListMetadata(name, description, public, region, language, mutationId))
        }
    }

    override suspend fun updateList(
        id: Int,
        name: String,
        description: String,
        public: Boolean,
        mutationId: String,
    ): MutationResult = response {
        client.put("$root/v2/account/lists/$id") {
            headers(authenticated = true, mutationId = mutationId); contentType(ContentType.Application.Json)
            setBody(ListMetadata(name, description, public, null, null, mutationId))
        }
    }

    override suspend fun deleteList(id: Int, mutationId: String): MutationResult = response {
        client.delete("$root/v2/account/lists/$id") { headers(authenticated = true, mutationId = mutationId) }
    }

    override suspend fun mutateListItems(
        id: Int,
        items: List<ListItemMutation>,
        remove: Boolean,
        mutationId: String,
    ): MutationResult = response {
        val path = "$root/v2/account/lists/$id/items"
        if (remove) client.delete(path) {
            headers(authenticated = true, mutationId = mutationId)
            contentType(ContentType.Application.Json)
            setBody(ListItemsMutation(items, mutationId))
        } else client.post(path) {
            headers(authenticated = true, mutationId = mutationId)
            contentType(ContentType.Application.Json)
            setBody(ListItemsMutation(items, mutationId))
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.headers(authenticated: Boolean = false, mutationId: String? = null) {
        applySessionRequestOptions()
        header("X-SmartMovie-Client", clientId)
        header("Accept", "application/json")
        if (authenticated) credentials.load()?.let { header("Authorization", "Bearer $it") }
        if (authenticated) csrfToken?.let { header("X-CSRF-Token", it) }
        mutationId?.let { header("Idempotency-Key", it) }
    }

    private suspend inline fun <reified T> response(crossinline call: suspend () -> io.ktor.client.statement.HttpResponse): T {
        val result = call()
        if (result.status.value !in 200..299) {
            val error = runCatching { format.decodeFromString<ErrorEnvelope>(result.bodyAsText()).error }.getOrNull()
            throw CatalogFailure(CatalogFailure.Kind.SERVER, error?.message ?: "The account service rejected the request.", error?.retryAfter, error?.requestId)
        }
        return result.body()
    }
}

@Serializable private data class AuthAttemptRequest(@SerialName("return_uri") val returnUri: String, val mode: String)
@Serializable private data class CompleteAuthRequest(@SerialName("attempt_id") val attemptId: String, @SerialName("device_code") val deviceCode: String?)
@Serializable private data class AuthAttemptStatus(val status: String)
@Serializable private data class LogoutResponse(val success: Boolean)
@Serializable private data class CSRFResponse(@SerialName("csrf_token") val csrfToken: String)
@Serializable private data class AccountLibraryMutation(@SerialName("media_id") val mediaId: Int, val enabled: Boolean, @SerialName("mutation_id") val mutationId: String)
@Serializable private data class RatingMutation(val value: Double, @SerialName("mutation_id") val mutationId: String)
@Serializable private data class ListMetadata(
    val name: String,
    val description: String,
    val public: Boolean,
    @SerialName("iso_3166_1") val region: String?,
    @SerialName("iso_639_1") val language: String?,
    @SerialName("mutation_id") val mutationId: String,
)
@Serializable data class ListItemMutation(@SerialName("media_type") val mediaType: String, @SerialName("media_id") val mediaId: Int, val comment: String? = null)
@Serializable private data class ListItemsMutation(val items: List<ListItemMutation>, @SerialName("mutation_id") val mutationId: String)
