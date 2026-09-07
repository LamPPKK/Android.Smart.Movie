package com.lamndt.smartmovie.multiplatform.data

import com.lamndt.smartmovie.multiplatform.model.DiscoverFilter
import com.lamndt.smartmovie.multiplatform.model.DiscoverConfiguration
import com.lamndt.smartmovie.multiplatform.model.Genre
import com.lamndt.smartmovie.multiplatform.model.HomeFeed
import com.lamndt.smartmovie.multiplatform.model.ImageConfiguration
import com.lamndt.smartmovie.multiplatform.model.MediaType
import com.lamndt.smartmovie.multiplatform.model.PagedResult
import com.lamndt.smartmovie.multiplatform.model.SearchScope
import com.lamndt.smartmovie.multiplatform.model.TitleDetail
import com.lamndt.smartmovie.multiplatform.model.TitleSummary
import com.lamndt.smartmovie.multiplatform.model.CapabilitiesV2
import com.lamndt.smartmovie.multiplatform.model.CatalogEntity
import com.lamndt.smartmovie.multiplatform.model.CollectionDetail
import com.lamndt.smartmovie.multiplatform.model.CreditDetail
import com.lamndt.smartmovie.multiplatform.model.EntityKind
import com.lamndt.smartmovie.multiplatform.model.EpisodeDetail
import com.lamndt.smartmovie.multiplatform.model.ExternalIdFindResult
import com.lamndt.smartmovie.multiplatform.model.ExternalIdSource
import com.lamndt.smartmovie.multiplatform.model.KeywordDetail
import com.lamndt.smartmovie.multiplatform.model.OrganizationDetail
import com.lamndt.smartmovie.multiplatform.model.PersonDetail
import com.lamndt.smartmovie.multiplatform.model.SearchScopeV2
import com.lamndt.smartmovie.multiplatform.model.SeasonDetail
import com.lamndt.smartmovie.multiplatform.model.TitleDetailV2
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLPathPart
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.pow
import kotlin.random.Random

interface CatalogApi {
    suspend fun home(mediaType: MediaType, language: String): HomeFeed
    suspend fun genres(mediaType: MediaType, language: String): List<Genre>
    suspend fun discover(mediaType: MediaType, filter: DiscoverFilter, page: Int, language: String): PagedResult<TitleSummary>
    suspend fun discoverBasic(
        mediaType: MediaType,
        filter: DiscoverFilter,
        page: Int,
        language: String,
    ): PagedResult<TitleSummary> = discover(mediaType, filter, page, language)
    suspend fun search(query: String, scope: SearchScope, page: Int, language: String): PagedResult<TitleSummary>
    suspend fun detail(mediaType: MediaType, id: Int, language: String): TitleDetail
    suspend fun imageConfiguration(): ImageConfiguration
}

interface CatalogApiV2 : CatalogApi {
    suspend fun capabilities(): CapabilitiesV2
    suspend fun discoverConfiguration(language: String, region: String?): DiscoverConfiguration
    suspend fun trending(kind: String, window: String, page: Int, language: String, includeAdult: Boolean): PagedResult<CatalogEntity>
    suspend fun searchEntities(
        query: String,
        scope: SearchScopeV2,
        page: Int,
        language: String,
        region: String?,
        includeAdult: Boolean,
    ): PagedResult<CatalogEntity>
    suspend fun findExternalId(
        externalId: String,
        source: ExternalIdSource,
        language: String,
        includeAdult: Boolean = false,
    ): ExternalIdFindResult
    suspend fun deepDetail(mediaType: MediaType, id: Int, language: String, region: String?, includeAdult: Boolean): TitleDetailV2
    suspend fun person(id: Int, language: String, includeAdult: Boolean = false): PersonDetail
    suspend fun collection(id: Int, language: String, includeAdult: Boolean = false): CollectionDetail
    suspend fun organization(
        kind: EntityKind,
        id: Int,
        language: String,
        page: Int,
        includeAdult: Boolean = false,
    ): OrganizationDetail
    suspend fun keyword(id: Int, language: String, page: Int, includeAdult: Boolean = false): KeywordDetail
    suspend fun season(seriesId: Int, number: Int, language: String): SeasonDetail
    suspend fun episode(seriesId: Int, season: Int, number: Int, language: String): EpisodeDetail
    suspend fun credit(id: String, language: String, includeAdult: Boolean = false): CreditDetail
}

class CatalogFailure(
    val kind: Kind,
    message: String,
    val retryAfterSeconds: Long? = null,
    val requestId: String? = null,
) : Exception(message) {
    enum class Kind { UNAUTHORIZED, NOT_FOUND, RATE_LIMITED, SERVER, DECODING, TRANSPORT }
}

class KtorCatalogApi(
    baseUrl: String = "https://catalog.smartmovie.app/",
    private val clientId: String,
    private val jsonFormat: Json = Json { ignoreUnknownKeys = true; explicitNulls = false },
    private val sleeper: suspend (Long) -> Unit = { delay(it) },
) : CatalogApiV2 {
    private val root = baseUrl.trimEnd('/')
    private val client = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) { json(jsonFormat) }
        install(HttpTimeout) {
            requestTimeoutMillis = 20_000
            connectTimeoutMillis = 10_000
        }
    }

    override suspend fun home(mediaType: MediaType, language: String): HomeFeed = request {
        client.get("$root/v2/home") {
            smartMovieHeaders()
            parameter("media_type", mediaType.wireValue)
            parameter("language", language)
        }
    }

    override suspend fun genres(mediaType: MediaType, language: String): List<Genre> = request<GenreEnvelope> {
        client.get("$root/v2/genres/${mediaType.wireValue}") {
            smartMovieHeaders()
            parameter("language", language)
        }
    }.genres

    override suspend fun discover(
        mediaType: MediaType,
        filter: DiscoverFilter,
        page: Int,
        language: String,
    ): PagedResult<TitleSummary> = request {
        client.get("$root/v2/discover/${mediaType.wireValue}") {
            smartMovieHeaders()
            parameter("page", page)
            parameter("language", language)
            parameter("sort_by", filter.sort.wireValue)
            parameter("vote_average_gte", filter.minimumRating)
            parameter("include_adult", filter.includeAdult)
            filter.genres.takeIf { it.isNotEmpty() }?.let { parameter("genres", it.sorted().joinToString(",")) }
            filter.year?.let { parameter("year", it) }
            filter.releaseDateFrom.normalized()?.let { parameter("release_date_gte", it) }
            filter.releaseDateThrough.normalized()?.let { parameter("release_date_lte", it) }
            filter.originalLanguage.normalized()?.lowercase()?.let { parameter("original_language", it) }
            filter.originCountry.normalized()?.uppercase()?.let { parameter("origin_country", it) }
            filter.minimumRuntime?.let { parameter("runtime_gte", it) }
            filter.maximumRuntime?.let { parameter("runtime_lte", it) }
            filter.minimumVoteCount.takeIf { it > 0 }?.let { parameter("vote_count_gte", it) }
            filter.region.normalized()?.uppercase()?.let { region ->
                if (mediaType == MediaType.MOVIE) parameter("region", region)
                if (filter.watchProviderIds.isNotEmpty() || filter.monetizationTypes.isNotEmpty()) {
                    parameter("watch_region", region)
                }
            }
            if (mediaType == MediaType.MOVIE) {
                filter.certificationCountry.normalized()?.uppercase()?.let { parameter("certification_country", it) }
                filter.certificationMinimum.normalized()?.let { parameter("certification_gte", it) }
                filter.certificationMaximum.normalized()?.let { parameter("certification_lte", it) }
            }
            filter.watchProviderIds.takeIf { it.isNotEmpty() }
                ?.let { parameter("watch_providers", it.sorted().joinToString("|")) }
            filter.monetizationTypes.takeIf { it.isNotEmpty() }
                ?.let { values -> parameter("watch_monetization_types", values.map { it.wireValue }.sorted().joinToString("|")) }
        }
    }

    override suspend fun discoverBasic(
        mediaType: MediaType,
        filter: DiscoverFilter,
        page: Int,
        language: String,
    ): PagedResult<TitleSummary> = request {
        client.get("$root/v1/discover/${mediaType.wireValue}") {
            smartMovieHeaders()
            parameter("page", page)
            parameter("language", language)
            parameter("sort_by", filter.sort.wireValue)
            parameter("vote_average_gte", filter.minimumRating)
            filter.genres.takeIf { it.isNotEmpty() }?.let { parameter("genre_ids", it.sorted().joinToString(",")) }
            filter.year?.let { parameter("year", it) }
        }
    }

    override suspend fun search(
        query: String,
        scope: SearchScope,
        page: Int,
        language: String,
    ): PagedResult<TitleSummary> = request {
        client.get("$root/v1/search") {
            smartMovieHeaders()
            parameter("query", query)
            parameter("scope", scope.wireValue)
            parameter("page", page)
            parameter("language", language)
        }
    }

    override suspend fun detail(mediaType: MediaType, id: Int, language: String): TitleDetail = request {
        client.get("$root/v1/titles/${mediaType.wireValue}/$id") {
            smartMovieHeaders()
            parameter("language", language)
        }
    }

    override suspend fun imageConfiguration(): ImageConfiguration = request {
        client.get("$root/v1/configuration") { smartMovieHeaders() }
    }

    override suspend fun capabilities(): CapabilitiesV2 = request {
        client.get("$root/v2/capabilities") { smartMovieHeaders() }
    }

    override suspend fun discoverConfiguration(language: String, region: String?): DiscoverConfiguration = request {
        client.get("$root/v2/configuration") {
            smartMovieHeaders()
            parameter("language", language)
            region.normalized()?.uppercase()?.let { parameter("region", it) }
        }
    }

    override suspend fun trending(
        kind: String,
        window: String,
        page: Int,
        language: String,
        includeAdult: Boolean,
    ): PagedResult<CatalogEntity> = request {
        client.get("$root/v2/trending/$kind/$window") {
            smartMovieHeaders(); parameter("page", page); parameter("language", language); parameter("include_adult", includeAdult)
        }
    }

    override suspend fun searchEntities(
        query: String,
        scope: SearchScopeV2,
        page: Int,
        language: String,
        region: String?,
        includeAdult: Boolean,
    ): PagedResult<CatalogEntity> = request {
        client.get("$root/v2/search") {
            smartMovieHeaders(); parameter("query", query); parameter("scope", scope.wireValue); parameter("page", page)
            parameter("language", language); region?.let { parameter("region", it) }; parameter("include_adult", includeAdult)
        }
    }

    override suspend fun findExternalId(
        externalId: String,
        source: ExternalIdSource,
        language: String,
        includeAdult: Boolean,
    ): ExternalIdFindResult = request {
        client.get("$root/v2/find/${externalId.encodeURLPathPart()}") {
            smartMovieHeaders(); parameter("source", source.wireValue); parameter("language", language)
            parameter("include_adult", includeAdult)
        }
    }

    override suspend fun deepDetail(
        mediaType: MediaType,
        id: Int,
        language: String,
        region: String?,
        includeAdult: Boolean,
    ): TitleDetailV2 = request {
        client.get("$root/v2/titles/${mediaType.wireValue}/$id") {
            smartMovieHeaders(); parameter("language", language); region?.let { parameter("region", it) }; parameter("include_adult", includeAdult)
        }
    }

    override suspend fun person(id: Int, language: String, includeAdult: Boolean): PersonDetail = request {
        client.get("$root/v2/entities/person/$id") {
            smartMovieHeaders(); parameter("language", language); parameter("include_adult", includeAdult)
        }
    }
    override suspend fun collection(id: Int, language: String, includeAdult: Boolean): CollectionDetail = request {
        client.get("$root/v2/entities/collection/$id") {
            smartMovieHeaders(); parameter("language", language); parameter("include_adult", includeAdult)
        }
    }
    override suspend fun organization(
        kind: EntityKind,
        id: Int,
        language: String,
        page: Int,
        includeAdult: Boolean,
    ): OrganizationDetail = request {
        require(kind == EntityKind.COMPANY || kind == EntityKind.NETWORK)
        client.get("$root/v2/entities/${kind.wireValue}/$id") {
            smartMovieHeaders(); parameter("language", language); parameter("page", page); parameter("include_adult", includeAdult)
        }
    }
    override suspend fun keyword(id: Int, language: String, page: Int, includeAdult: Boolean): KeywordDetail = request {
        client.get("$root/v2/entities/keyword/$id") {
            smartMovieHeaders(); parameter("language", language); parameter("page", page); parameter("include_adult", includeAdult)
        }
    }
    override suspend fun season(seriesId: Int, number: Int, language: String): SeasonDetail = request {
        client.get("$root/v2/tv/$seriesId/seasons/$number") { smartMovieHeaders(); parameter("language", language) }
    }
    override suspend fun episode(seriesId: Int, season: Int, number: Int, language: String): EpisodeDetail = request {
        client.get("$root/v2/tv/$seriesId/seasons/$season/episodes/$number") { smartMovieHeaders(); parameter("language", language) }
    }
    override suspend fun credit(id: String, language: String, includeAdult: Boolean): CreditDetail = request {
        client.get("$root/v2/credits/${id.encodeURLPathPart()}") {
            smartMovieHeaders(); parameter("language", language); parameter("include_adult", includeAdult)
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.smartMovieHeaders() {
        header("X-SmartMovie-Client", clientId)
        header("Accept", "application/json")
    }

    private suspend inline fun <reified T> request(crossinline call: suspend () -> HttpResponse): T {
        var lastFailure: Throwable? = null
        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                val response = call()
                val payload = response.bodyAsText()
                if (response.status.value in 200..299) {
                    return try {
                        jsonFormat.decodeFromString(payload)
                    } catch (failure: Throwable) {
                        throw CatalogFailure(CatalogFailure.Kind.DECODING, "The catalog returned unreadable data.")
                    }
                }

                val error = runCatching { jsonFormat.decodeFromString<ErrorEnvelope>(payload).error }.getOrNull()
                when (response.status) {
                    HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> throw CatalogFailure(
                        CatalogFailure.Kind.UNAUTHORIZED,
                        "The catalog service is not configured correctly.",
                        requestId = error?.requestId,
                    )
                    HttpStatusCode.NotFound -> throw CatalogFailure(
                        CatalogFailure.Kind.NOT_FOUND,
                        "This title is no longer available.",
                        requestId = error?.requestId,
                    )
                    HttpStatusCode.TooManyRequests -> {
                        val retryAfter = error?.retryAfter
                            ?: response.headers["Retry-After"]?.toLongOrNull()
                            ?: (attempt + 1).toLong()
                        if (attempt < MAX_ATTEMPTS - 1) sleeper(retryAfter.coerceAtLeast(0) * 1_000)
                        else throw CatalogFailure(
                            CatalogFailure.Kind.RATE_LIMITED,
                            "Too many requests. Please try again shortly.",
                            retryAfter,
                            error?.requestId,
                        )
                    }
                    else -> if (response.status.value >= 500) {
                        if (attempt < MAX_ATTEMPTS - 1) sleeper(backoffMillis(attempt))
                        else throw CatalogFailure(
                            CatalogFailure.Kind.SERVER,
                            "The catalog is temporarily unavailable.",
                            requestId = error?.requestId,
                        )
                    } else {
                        throw CatalogFailure(
                            CatalogFailure.Kind.SERVER,
                            error?.message ?: "The catalog rejected the request.",
                            requestId = error?.requestId,
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (known: CatalogFailure) {
                throw known
            } catch (failure: Throwable) {
                lastFailure = failure
                if (attempt < MAX_ATTEMPTS - 1) sleeper(backoffMillis(attempt))
            }
        }
        throw CatalogFailure(
            CatalogFailure.Kind.TRANSPORT,
            lastFailure?.message ?: "Check your connection and try again.",
        )
    }

    private fun backoffMillis(attempt: Int): Long = (300.0 * 2.0.pow(attempt)).toLong()

    companion object { private const val MAX_ATTEMPTS = 3 }
}

private fun String?.normalized(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

@Serializable
private data class GenreEnvelope(val genres: List<Genre>)

@Serializable
internal data class ErrorEnvelope(val error: ErrorBody)

@Serializable
internal data class ErrorBody(
    val code: String,
    val message: String,
    @kotlinx.serialization.SerialName("request_id") val requestId: String? = null,
    @kotlinx.serialization.SerialName("retry_after") val retryAfter: Long? = null,
)

fun createInstallationId(): String {
    val bytes = Random.nextBytes(16)
    bytes[6] = ((bytes[6].toInt() and 0x0f) or 0x40).toByte()
    bytes[8] = ((bytes[8].toInt() and 0x3f) or 0x80).toByte()
    val hex = bytes.joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
    return "${hex.take(8)}-${hex.substring(8, 12)}-${hex.substring(12, 16)}-${hex.substring(16, 20)}-${hex.drop(20)}"
}
