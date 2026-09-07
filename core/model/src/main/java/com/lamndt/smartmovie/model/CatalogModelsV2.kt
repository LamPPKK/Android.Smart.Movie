package com.lamndt.smartmovie.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
enum class EntityKind {
    @SerialName("movie") MOVIE,
    @SerialName("tv") TV,
    @SerialName("person") PERSON,
    @SerialName("collection") COLLECTION,
    @SerialName("company") COMPANY,
    @SerialName("network") NETWORK,
    @SerialName("keyword") KEYWORD,
    @SerialName("season") SEASON,
    @SerialName("episode") EPISODE;

    val wireValue: String get() = name.lowercase()
}

enum class SearchScopeV2(val wireValue: String) {
    ALL("all"), MOVIE("movie"), TV("tv"), PERSON("person"), COLLECTION("collection"), COMPANY("company"), KEYWORD("keyword")
}

enum class CatalogSearchMode { CATALOG, EXTERNAL_ID }

@Serializable
enum class ExternalIdSource(val wireValue: String, val displayName: String, val example: String) {
    @SerialName("imdb_id") IMDB("imdb_id", "IMDb", "tt0133093"),
    @SerialName("tvdb_id") TVDB("tvdb_id", "TheTVDB", "73739"),
    @SerialName("wikidata_id") WIKIDATA("wikidata_id", "Wikidata", "Q83495"),
    @SerialName("facebook_id") FACEBOOK("facebook_id", "Facebook", "TheMatrixMovie"),
    @SerialName("instagram_id") INSTAGRAM("instagram_id", "Instagram", "thematrixmovie"),
    @SerialName("twitter_id") TWITTER("twitter_id", "X / Twitter", "TheMatrixMovie"),
}

@Serializable(with = CatalogEntitySerializer::class)
sealed interface CatalogEntity {
    val entityKind: EntityKind
    val stableKey: String

    data class Title(val value: TitleSummary) : CatalogEntity {
        override val entityKind = if (value.mediaType == MediaType.MOVIE) EntityKind.MOVIE else EntityKind.TV
        override val stableKey = value.libraryKey
    }

    data class Person(val value: PersonSummary) : CatalogEntity {
        override val entityKind = EntityKind.PERSON
        override val stableKey = "person:${value.id}"
    }

    data class Collection(val value: CollectionSummary) : CatalogEntity {
        override val entityKind = EntityKind.COLLECTION
        override val stableKey = "collection:${value.id}"
    }

    data class Organization(val value: OrganizationSummary) : CatalogEntity {
        override val entityKind = value.entityKind
        override val stableKey = "${value.entityKind.wireValue}:${value.id}"
    }

    data class Keyword(val value: KeywordSummary) : CatalogEntity {
        override val entityKind = EntityKind.KEYWORD
        override val stableKey = "keyword:${value.id}"
    }

    data class Season(val value: SeasonSummary) : CatalogEntity {
        override val entityKind = EntityKind.SEASON
        override val stableKey = "season:${value.id}"
    }

    data class Episode(val value: EpisodeSummary) : CatalogEntity {
        override val entityKind = EntityKind.EPISODE
        override val stableKey = "episode:${value.id}"
    }
}

object CatalogEntitySerializer : KSerializer<CatalogEntity> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CatalogEntity")

    override fun deserialize(decoder: Decoder): CatalogEntity {
        val input = decoder as? JsonDecoder ?: throw SerializationException("CatalogEntity requires JSON")
        val element = input.decodeJsonElement()
        val kind = (element as? JsonObject)?.get("entity_kind")?.let { (it as? JsonPrimitive)?.content }
            ?: throw SerializationException("CatalogEntity is missing entity_kind")
        return when (kind) {
            "movie", "tv" -> CatalogEntity.Title(input.json.decodeFromJsonElement(element))
            "person" -> CatalogEntity.Person(input.json.decodeFromJsonElement(element))
            "collection" -> CatalogEntity.Collection(input.json.decodeFromJsonElement(element))
            "company", "network" -> CatalogEntity.Organization(input.json.decodeFromJsonElement(element))
            "keyword" -> CatalogEntity.Keyword(input.json.decodeFromJsonElement(element))
            "season" -> CatalogEntity.Season(input.json.decodeFromJsonElement(element))
            "episode" -> CatalogEntity.Episode(input.json.decodeFromJsonElement(element))
            else -> throw SerializationException("Unsupported entity_kind: $kind")
        }
    }

    override fun serialize(encoder: Encoder, value: CatalogEntity) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("CatalogEntity requires JSON")
        val payload = when (value) {
            is CatalogEntity.Title -> output.json.encodeToJsonElement(value.value)
            is CatalogEntity.Person -> output.json.encodeToJsonElement(value.value)
            is CatalogEntity.Collection -> output.json.encodeToJsonElement(value.value)
            is CatalogEntity.Organization -> output.json.encodeToJsonElement(value.value)
            is CatalogEntity.Keyword -> output.json.encodeToJsonElement(value.value)
            is CatalogEntity.Season -> output.json.encodeToJsonElement(value.value)
            is CatalogEntity.Episode -> output.json.encodeToJsonElement(value.value)
        } as JsonObject
        output.encodeJsonElement(JsonObject(mapOf("entity_kind" to JsonPrimitive(value.entityKind.wireValue)) + payload))
    }
}

@Serializable
data class ExternalIdFindResult(
    val source: ExternalIdSource,
    @SerialName("external_id") val externalId: String,
    val results: List<CatalogEntity>,
)

@Serializable
data class PersonSummary(
    val id: Int,
    val name: String,
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("known_for_department") val knownForDepartment: String? = null,
    val popularity: Double = 0.0,
    @SerialName("known_for") val knownFor: List<TitleSummary> = emptyList(),
)

@Serializable
data class CollectionSummary(
    val id: Int,
    val name: String,
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
)

@Serializable
data class OrganizationSummary(
    @SerialName("entity_kind") val entityKind: EntityKind,
    val id: Int,
    val name: String,
    @SerialName("logo_path") val logoPath: String? = null,
    @SerialName("origin_country") val originCountry: String? = null,
)

@Serializable data class KeywordSummary(val id: Int, val name: String)

@Serializable
data class SeasonSummary(
    val id: Int,
    @SerialName("series_id") val seriesId: Int? = null,
    @SerialName("season_number") val seasonNumber: Int,
    val name: String,
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("air_date") val airDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("episode_count") val episodeCount: Int = 0,
)

@Serializable
data class EpisodeSummary(
    val id: Int,
    @SerialName("series_id") val seriesId: Int,
    @SerialName("season_number") val seasonNumber: Int,
    @SerialName("episode_number") val episodeNumber: Int,
    val name: String,
    val overview: String = "",
    @SerialName("still_path") val stillPath: String? = null,
    @SerialName("air_date") val airDate: String? = null,
    @SerialName("runtime_minutes") val runtimeMinutes: Int? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
) { val episodeKey: String get() = "$seriesId:$seasonNumber:$episodeNumber" }

@Serializable
data class Credit(
    @SerialName("credit_id") val creditId: String? = null,
    val id: Int? = null,
    @SerialName("media_type") val mediaType: MediaType? = null,
    val title: String? = null,
    val character: String? = null,
    val job: String? = null,
    val department: String? = null,
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    val order: Int? = null,
    @SerialName("episode_count") val episodeCount: Int? = null,
    val adult: Boolean = false,
)

fun visibleCatalogTitles(values: List<TitleSummary>, includeAdult: Boolean): List<TitleSummary> =
    values.filter { includeAdult || !it.adult }

fun visibleCatalogCredits(values: List<Credit>, includeAdult: Boolean): List<Credit> =
    values.filter { includeAdult || !it.adult }

fun visibleCatalogEntities(values: List<CatalogEntity>, includeAdult: Boolean): List<CatalogEntity> =
    values.mapNotNull { entity ->
        when (entity) {
            is CatalogEntity.Title -> entity.takeIf { includeAdult || !entity.value.adult }
            is CatalogEntity.Person -> CatalogEntity.Person(
                entity.value.copy(knownFor = visibleCatalogTitles(entity.value.knownFor, includeAdult)),
            )
            is CatalogEntity.Season, is CatalogEntity.Episode -> entity.takeIf { includeAdult }
            else -> entity
        }
    }

@Serializable
data class CreditDetail(
    @SerialName("credit_id") val creditId: String,
    @SerialName("credit_type") val creditType: String? = null,
    val department: String? = null,
    val job: String? = null,
    val character: String? = null,
    @SerialName("person_summary") val personSummary: PersonSummary? = null,
    @SerialName("title_summary") val titleSummary: TitleSummary? = null,
)

@Serializable
data class ImageAsset(
    val kind: String,
    @SerialName("file_path") val filePath: String,
    @SerialName("aspect_ratio") val aspectRatio: Double,
    val height: Int,
    val width: Int,
    val language: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
)

@Serializable
data class ImageGroup(
    val backdrops: List<ImageAsset> = emptyList(),
    val posters: List<ImageAsset> = emptyList(),
    val logos: List<ImageAsset> = emptyList(),
)

@Serializable
data class ProviderOffer(
    @SerialName("provider_id") val providerId: Int,
    @SerialName("provider_name") val providerName: String,
    @SerialName("logo_path") val logoPath: String? = null,
    @SerialName("display_priority") val displayPriority: Int = 0,
)

@Serializable
data class WatchProviderOption(
    val id: Int,
    val name: String,
    @SerialName("logo_path") val logoPath: String? = null,
    @SerialName("display_priority") val displayPriority: Int = 0,
)

@Serializable
data class ConfigurationCountry(
    @SerialName("iso_3166_1") val code: String,
    @SerialName("english_name") val englishName: String,
    @SerialName("native_name") val nativeName: String? = null,
) {
    val displayName: String get() = nativeName?.takeIf(String::isNotBlank) ?: englishName
}

@Serializable
data class ConfigurationLanguage(
    @SerialName("iso_639_1") val code: String,
    @SerialName("english_name") val englishName: String,
    val name: String? = null,
) {
    val displayName: String get() = name?.takeIf(String::isNotBlank) ?: englishName
}

@Serializable
data class WatchProviderOptions(
    val movie: List<WatchProviderOption> = emptyList(),
    val tv: List<WatchProviderOption> = emptyList(),
) {
    fun values(mediaType: MediaType): List<WatchProviderOption> = if (mediaType == MediaType.MOVIE) movie else tv
}

@Serializable
data class DiscoverConfiguration(
    val countries: List<ConfigurationCountry> = emptyList(),
    val languages: List<ConfigurationLanguage> = emptyList(),
    @SerialName("watch_provider_regions") val watchProviderRegions: List<ConfigurationCountry> = emptyList(),
    val region: String? = null,
    @SerialName("watch_providers") val watchProviders: WatchProviderOptions? = null,
)

@Serializable
data class ProviderRegion(
    val region: String,
    @SerialName("tmdb_url") val tmdbUrl: String? = null,
    val attribution: String,
    val stream: List<ProviderOffer> = emptyList(),
    val rent: List<ProviderOffer> = emptyList(),
    val buy: List<ProviderOffer> = emptyList(),
    val ads: List<ProviderOffer> = emptyList(),
    val free: List<ProviderOffer> = emptyList(),
)

@Serializable
data class Review(
    val id: String,
    val author: String,
    val content: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val url: String? = null,
    @SerialName("avatar_path") val avatarPath: String? = null,
    val rating: Double? = null,
)

@Serializable
data class AlternativeTitle(
    @SerialName("iso_3166_1") val countryCode: String? = null,
    val title: String = "",
    val type: String? = null,
)

@Serializable
data class ReleaseEvent(
    val certification: String = "",
    val descriptors: List<String> = emptyList(),
    @SerialName("iso_639_1") val languageCode: String? = null,
    val note: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val type: Int? = null,
)

@Serializable
data class ReleaseInformation(
    @SerialName("iso_3166_1") val countryCode: String = "",
    @SerialName("release_dates") val releaseDates: List<ReleaseEvent> = emptyList(),
    val rating: String? = null,
    val descriptors: List<String> = emptyList(),
) {
    val certification: String?
        get() = rating?.takeIf(String::isNotBlank)
            ?: releaseDates.firstNotNullOfOrNull { event -> event.certification.takeIf(String::isNotBlank) }
    val firstReleaseDate: String? get() = releaseDates.mapNotNull(ReleaseEvent::releaseDate).minOrNull()
}

@Serializable
data class TitleTranslationData(
    val title: String? = null,
    val name: String? = null,
    val overview: String? = null,
    val homepage: String? = null,
    val tagline: String? = null,
    val runtime: Int? = null,
)

@Serializable
data class TitleTranslation(
    @SerialName("iso_639_1") val languageCode: String = "",
    @SerialName("iso_3166_1") val countryCode: String = "",
    @SerialName("name") val languageName: String = "",
    @SerialName("english_name") val englishName: String = "",
    val data: TitleTranslationData = TitleTranslationData(),
) {
    val localizedTitle: String?
        get() = data.title?.takeIf(String::isNotBlank) ?: data.name?.takeIf(String::isNotBlank)
}

@Serializable
data class TitleDetailV2(
    val id: Int,
    @SerialName("media_type") val mediaType: MediaType,
    val title: String,
    @SerialName("original_title") val originalTitle: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    val tagline: String = "",
    val homepage: String? = null,
    @SerialName("original_language") val originalLanguage: String? = null,
    @SerialName("origin_countries") val originCountries: List<String> = emptyList(),
    val adult: Boolean = false,
    val popularity: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("runtime_minutes") val runtimeMinutes: Int? = null,
    @SerialName("number_of_seasons") val numberOfSeasons: Int? = null,
    val status: String? = null,
    val budget: Long? = null,
    val revenue: Long? = null,
    val genres: List<Genre> = emptyList(),
    val creators: List<Credit> = emptyList(),
    val cast: List<Credit> = emptyList(),
    val crew: List<Credit> = emptyList(),
    val collection: CollectionSummary? = null,
    val companies: List<OrganizationSummary> = emptyList(),
    val networks: List<OrganizationSummary> = emptyList(),
    val seasons: List<SeasonSummary> = emptyList(),
    @SerialName("alternative_titles") val alternativeTitles: List<AlternativeTitle> = emptyList(),
    @SerialName("external_ids") val externalIds: Map<String, String> = emptyMap(),
    val images: ImageGroup = ImageGroup(),
    val videos: List<Video> = emptyList(),
    val reviews: PagedResult<Review> = PagedResult(1, 0, emptyList()),
    val recommendations: PagedResult<TitleSummary> = PagedResult(1, 0, emptyList()),
    val similar: List<TitleSummary> = emptyList(),
    @SerialName("release_information") val releaseInformation: List<ReleaseInformation> = emptyList(),
    val translations: List<TitleTranslation> = emptyList(),
    @SerialName("watch_providers") val watchProviders: List<ProviderRegion> = emptyList(),
    val keywords: List<KeywordSummary> = emptyList(),
) {
    val summary get() = TitleSummary(id, mediaType, title, originalTitle, overview, posterPath, backdropPath, releaseDate, voteAverage, genreIds, adult)

    fun releaseInformationFor(region: String?): ReleaseInformation? {
        val normalized = region?.trim()?.uppercase()
        return releaseInformation.firstOrNull { it.countryCode.uppercase() == normalized }
    }

    fun displayAlternativeTitles(region: String?): List<AlternativeTitle> {
        val normalized = region?.trim()?.uppercase()
        return alternativeTitles
            .filter { it.title.isNotBlank() }
            .sortedByDescending { it.countryCode?.uppercase() == normalized }
            .distinctBy { it.title.lowercase() }
    }

    fun displayTranslations(language: String?): List<TitleTranslation> {
        val normalized = language?.substringBefore('-')?.lowercase()
        return translations
            .filter { it.localizedTitle != null }
            .sortedByDescending { it.languageCode.lowercase() == normalized }
            .distinctBy { "${it.languageCode.lowercase()}:${it.localizedTitle?.lowercase()}" }
    }
}

@Serializable
data class CombinedCredits(val cast: List<Credit> = emptyList(), val crew: List<Credit> = emptyList())

@Serializable
data class PersonDetail(
    val id: Int,
    val name: String,
    val biography: String = "",
    val birthday: String? = null,
    val deathday: String? = null,
    @SerialName("place_of_birth") val placeOfBirth: String? = null,
    val homepage: String? = null,
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("known_for_department") val knownForDepartment: String? = null,
    val popularity: Double = 0.0,
    @SerialName("known_for") val knownFor: List<TitleSummary> = emptyList(),
    @SerialName("also_known_as") val alsoKnownAs: List<String> = emptyList(),
    val images: List<ImageAsset> = emptyList(),
    val credits: CombinedCredits = CombinedCredits(),
    @SerialName("external_ids") val externalIds: Map<String, String> = emptyMap(),
)

@Serializable
data class CollectionDetail(
    val id: Int,
    val name: String,
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    val parts: List<TitleSummary> = emptyList(),
    val images: ImageGroup = ImageGroup(),
)

@Serializable
data class OrganizationDetail(
    @SerialName("entity_kind") val entityKind: EntityKind,
    val id: Int,
    val name: String,
    @SerialName("logo_path") val logoPath: String? = null,
    @SerialName("origin_country") val originCountry: String? = null,
    val description: String = "",
    val headquarters: String? = null,
    val homepage: String? = null,
    @SerialName("parent_company") val parentCompany: OrganizationSummary? = null,
    val titles: PagedResult<TitleSummary>,
)

@Serializable data class KeywordDetail(val id: Int, val name: String, val titles: PagedResult<TitleSummary>)

@Serializable
data class SeasonDetail(
    val id: Int,
    @SerialName("series_id") val seriesId: Int,
    @SerialName("season_number") val seasonNumber: Int,
    val name: String,
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("air_date") val airDate: String? = null,
    @SerialName("episode_count") val episodeCount: Int = 0,
    val episodes: List<EpisodeSummary> = emptyList(),
    val credits: CombinedCredits = CombinedCredits(),
    val images: List<ImageAsset> = emptyList(),
    val videos: List<Video> = emptyList(),
    @SerialName("external_ids") val externalIds: Map<String, String> = emptyMap(),
)

@Serializable
data class EpisodeDetail(
    val id: Int,
    @SerialName("series_id") val seriesId: Int,
    @SerialName("season_number") val seasonNumber: Int,
    @SerialName("episode_number") val episodeNumber: Int,
    val name: String,
    val overview: String = "",
    @SerialName("still_path") val stillPath: String? = null,
    @SerialName("air_date") val airDate: String? = null,
    @SerialName("runtime_minutes") val runtimeMinutes: Int? = null,
    @SerialName("production_code") val productionCode: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("vote_count") val voteCount: Int = 0,
    val crew: List<Credit> = emptyList(),
    @SerialName("guest_stars") val guestStars: List<Credit> = emptyList(),
    val images: List<ImageAsset> = emptyList(),
    val videos: List<Video> = emptyList(),
    @SerialName("external_ids") val externalIds: Map<String, String> = emptyMap(),
)

@Serializable
data class CapabilitiesV2(
    @SerialName("api_version") val apiVersion: String,
    @SerialName("release_train") val releaseTrain: String,
    val catalog: Map<String, Boolean>,
    val account: Map<String, Boolean>,
    @SerialName("supported_languages") val supportedLanguages: List<String>,
    @SerialName("supported_entity_kinds") val supportedEntityKinds: List<EntityKind>,
    @SerialName("adult_content") val adultContent: AdultContentCapability,
) {
    fun supportsCatalog(capability: String): Boolean = catalog[capability] == true
    fun supportsAccount(capability: String): Boolean = account[capability] == true
}

fun CapabilitiesV2?.supportsAccountAuthentication(isTv: Boolean): Boolean =
    this?.supportsAccount(if (isTv) "tv_qr_auth" else "browser_auth") == true

@Serializable
data class AdultContentCapability(
    val supported: Boolean,
    @SerialName("default_enabled") val defaultEnabled: Boolean,
    @SerialName("local_pin_required") val localPinRequired: Boolean,
)

@Serializable
data class AccountProfile(
    val id: Int,
    val username: String,
    val name: String,
    val language: String? = null,
    val country: String? = null,
    @SerialName("include_adult") val includeAdult: Boolean = false,
    @SerialName("avatar_path") val avatarPath: String? = null,
    @SerialName("gravatar_hash") val gravatarHash: String? = null,
)

@Serializable
data class AuthAttempt(
    @SerialName("attempt_id") val attemptId: String,
    val status: String,
    @SerialName("authorization_url") val authorizationUrl: String,
    @SerialName("device_code") val deviceCode: String? = null,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("polling_interval") val pollingInterval: Int? = null,
)

@Serializable
data class AuthSession(
    @SerialName("session_token") val sessionToken: String? = null,
    @SerialName("csrf_token") val csrfToken: String,
    @SerialName("expires_at") val expiresAt: String,
    val profile: AccountProfile,
)

@Serializable
data class MutationResult(
    @SerialName("mutation_id") val mutationId: String,
    val success: Boolean? = null,
    @SerialName("status_code") val statusCode: Int? = null,
    @SerialName("status_message") val statusMessage: String? = null,
    @SerialName("list_id") val listId: Int? = null,
)

@Serializable
data class TitleAccountState(
    @SerialName("media_type") val mediaType: MediaType,
    @SerialName("media_id") val mediaId: Int,
    val favorite: Boolean,
    val watchlist: Boolean,
    val rated: JsonElement,
) {
    val ratingValue: Double? get() = rated.ratingValue()
}

@Serializable
data class EpisodeAccountState(
    @SerialName("series_id") val seriesId: Int,
    @SerialName("season_number") val seasonNumber: Int,
    @SerialName("episode_number") val episodeNumber: Int,
    val rated: JsonElement,
) {
    val ratingValue: Double? get() = rated.ratingValue()
}

private fun JsonElement.ratingValue(): Double? = (this as? JsonObject)
    ?.get("value")
    ?.jsonPrimitive
    ?.doubleOrNull

@Serializable
data class UserList(
    val id: Int,
    val name: String,
    val description: String = "",
    val public: Boolean = false,
    val page: Int? = null,
    @SerialName("total_pages") val totalPages: Int? = null,
    val results: List<TitleSummary> = emptyList(),
)
