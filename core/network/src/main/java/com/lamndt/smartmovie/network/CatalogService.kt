package com.lamndt.smartmovie.network

import com.lamndt.smartmovie.model.Genre
import com.lamndt.smartmovie.model.AccountProfile
import com.lamndt.smartmovie.model.AuthAttempt
import com.lamndt.smartmovie.model.AuthSession
import com.lamndt.smartmovie.model.CapabilitiesV2
import com.lamndt.smartmovie.model.CatalogEntity
import com.lamndt.smartmovie.model.CollectionDetail
import com.lamndt.smartmovie.model.CreditDetail
import com.lamndt.smartmovie.model.DiscoverConfiguration
import com.lamndt.smartmovie.model.EpisodeDetail
import com.lamndt.smartmovie.model.HomeFeed
import com.lamndt.smartmovie.model.ImageConfiguration
import com.lamndt.smartmovie.model.PagedResult
import com.lamndt.smartmovie.model.KeywordDetail
import com.lamndt.smartmovie.model.MutationResult
import com.lamndt.smartmovie.model.OrganizationDetail
import com.lamndt.smartmovie.model.PersonDetail
import com.lamndt.smartmovie.model.SeasonDetail
import com.lamndt.smartmovie.model.TitleDetail
import com.lamndt.smartmovie.model.TitleDetailV2
import com.lamndt.smartmovie.model.TitleSummary
import com.lamndt.smartmovie.model.TitleAccountState
import com.lamndt.smartmovie.model.EpisodeAccountState
import com.lamndt.smartmovie.model.ExternalIdFindResult
import com.lamndt.smartmovie.model.UserList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.HTTP
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.QueryMap

internal interface CatalogService {
    @GET("v1/home")
    suspend fun home(
        @Header(CLIENT_HEADER) clientId: String,
        @Query("media_type") mediaType: String,
        @Query("language") language: String,
    ): HomeFeed

    @GET("v2/genres/{mediaType}")
    suspend fun genres(
        @Header(CLIENT_HEADER) clientId: String,
        @Path("mediaType") mediaType: String,
        @Query("language") language: String,
    ): GenreEnvelope

    @GET("v2/discover/{mediaType}")
    suspend fun discover(
        @Header(CLIENT_HEADER) clientId: String,
        @Path("mediaType") mediaType: String,
        @QueryMap query: Map<String, String>,
    ): PagedResult<TitleSummary>

    @GET("v1/discover/{mediaType}")
    suspend fun discoverBasic(
        @Header(CLIENT_HEADER) clientId: String,
        @Path("mediaType") mediaType: String,
        @QueryMap query: Map<String, String>,
    ): PagedResult<TitleSummary>

    @GET("v1/search")
    suspend fun search(
        @Header(CLIENT_HEADER) clientId: String,
        @Query("query") query: String,
        @Query("scope") scope: String,
        @Query("page") page: Int,
        @Query("language") language: String,
    ): PagedResult<TitleSummary>

    @GET("v1/titles/{mediaType}/{id}")
    suspend fun detail(
        @Header(CLIENT_HEADER) clientId: String,
        @Path("mediaType") mediaType: String,
        @Path("id") id: Int,
        @Query("language") language: String,
    ): TitleDetail

    @GET("v1/configuration")
    suspend fun imageConfiguration(@Header(CLIENT_HEADER) clientId: String): ImageConfiguration

    @GET("v2/capabilities")
    suspend fun capabilities(@Header(CLIENT_HEADER) clientId: String): CapabilitiesV2

    @GET("v2/configuration")
    suspend fun discoverConfiguration(
        @Header(CLIENT_HEADER) clientId: String,
        @Query("language") language: String,
        @Query("region") region: String?,
    ): DiscoverConfiguration

    @GET("v2/trending/{kind}/{window}")
    suspend fun trending(
        @Header(CLIENT_HEADER) clientId: String,
        @Path("kind") kind: String,
        @Path("window") window: String,
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("include_adult") includeAdult: Boolean,
    ): PagedResult<CatalogEntity>

    @GET("v2/search")
    suspend fun searchEntities(
        @Header(CLIENT_HEADER) clientId: String,
        @Query("query") query: String,
        @Query("scope") scope: String,
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("region") region: String?,
        @Query("include_adult") includeAdult: Boolean,
    ): PagedResult<CatalogEntity>

    @GET("v2/find/{externalId}")
    suspend fun findExternalId(
        @Header(CLIENT_HEADER) clientId: String,
        @Path("externalId") externalId: String,
        @Query("source") source: String,
        @Query("language") language: String,
        @Query("include_adult") includeAdult: Boolean,
    ): ExternalIdFindResult

    @GET("v2/titles/{mediaType}/{id}")
    suspend fun deepDetail(
        @Header(CLIENT_HEADER) clientId: String,
        @Path("mediaType") mediaType: String,
        @Path("id") id: Int,
        @Query("language") language: String,
        @Query("region") region: String?,
        @Query("include_adult") includeAdult: Boolean,
    ): TitleDetailV2

    @GET("v2/entities/person/{id}") suspend fun person(
        @Header(CLIENT_HEADER) clientId: String, @Path("id") id: Int, @Query("language") language: String,
        @Query("include_adult") includeAdult: Boolean,
    ): PersonDetail
    @GET("v2/entities/collection/{id}") suspend fun collection(
        @Header(CLIENT_HEADER) clientId: String, @Path("id") id: Int, @Query("language") language: String,
        @Query("include_adult") includeAdult: Boolean,
    ): CollectionDetail
    @GET("v2/entities/{kind}/{id}") suspend fun organization(
        @Header(CLIENT_HEADER) clientId: String, @Path("kind") kind: String, @Path("id") id: Int,
        @Query("language") language: String, @Query("page") page: Int, @Query("include_adult") includeAdult: Boolean,
    ): OrganizationDetail
    @GET("v2/entities/keyword/{id}") suspend fun keyword(
        @Header(CLIENT_HEADER) clientId: String, @Path("id") id: Int, @Query("language") language: String,
        @Query("page") page: Int, @Query("include_adult") includeAdult: Boolean,
    ): KeywordDetail
    @GET("v2/tv/{seriesId}/seasons/{number}") suspend fun season(
        @Header(CLIENT_HEADER) clientId: String, @Path("seriesId") seriesId: Int, @Path("number") number: Int,
        @Query("language") language: String,
    ): SeasonDetail
    @GET("v2/tv/{seriesId}/seasons/{season}/episodes/{number}") suspend fun episode(
        @Header(CLIENT_HEADER) clientId: String, @Path("seriesId") seriesId: Int, @Path("season") season: Int,
        @Path("number") number: Int, @Query("language") language: String,
    ): EpisodeDetail
    @GET("v2/credits/{creditId}") suspend fun credit(
        @Header(CLIENT_HEADER) clientId: String,
        @Path("creditId") creditId: String,
        @Query("language") language: String,
        @Query("include_adult") includeAdult: Boolean,
    ): CreditDetail

    @POST("v2/auth/attempts") suspend fun createAuthAttempt(
        @Header(CLIENT_HEADER) clientId: String, @Body body: AuthAttemptRequest,
    ): AuthAttempt
    @GET("v2/auth/attempts/{id}") suspend fun authAttempt(
        @Header(CLIENT_HEADER) clientId: String, @Path("id") id: String, @Query("device_code") deviceCode: String?,
    ): AuthAttemptStatus
    @POST("v2/auth/complete") suspend fun completeAuth(
        @Header(CLIENT_HEADER) clientId: String, @Body body: CompleteAuthRequest,
    ): AuthSession
    @GET("v2/account/profile") suspend fun profile(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
    ): AccountProfile
    @GET("v2/account/state/{mediaType}/{id}") suspend fun accountState(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
        @Path("mediaType") mediaType: String, @Path("id") id: Int,
    ): TitleAccountState
    @GET("v2/account/state/episode/{seriesId}/{season}/{episode}") suspend fun episodeAccountState(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
        @Path("seriesId") seriesId: Int, @Path("season") season: Int, @Path("episode") episode: Int,
    ): EpisodeAccountState
    @POST("v2/auth/logout") suspend fun logout(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
    ): LogoutResult
    @GET("v2/account/{collection}/{mediaType}") suspend fun accountLibrary(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
        @Path("collection") collection: String, @Path("mediaType") mediaType: String,
        @Query("page") page: Int, @Query("language") language: String, @Query("sort_by") sortBy: String,
    ): PagedResult<TitleSummary>
    @PUT("v2/account/{collection}/{mediaType}") suspend fun setLibrary(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
        @Path("collection") collection: String, @Path("mediaType") mediaType: String, @Body body: LibraryMutation,
    ): MutationResult
    @PUT("v2/account/ratings/{mediaType}/{id}") suspend fun setRating(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
        @Path("mediaType") mediaType: String, @Path("id") id: Int, @Body body: RatingMutation,
    ): MutationResult
    @DELETE("v2/account/ratings/{mediaType}/{id}") suspend fun deleteRating(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Path("mediaType") mediaType: String, @Path("id") id: Int,
    ): MutationResult
    @PUT("v2/account/ratings/episode/{seriesId}/{season}/{episode}") suspend fun setEpisodeRating(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
        @Path("seriesId") seriesId: Int, @Path("season") season: Int, @Path("episode") episode: Int,
        @Body body: RatingMutation,
    ): MutationResult
    @DELETE("v2/account/ratings/episode/{seriesId}/{season}/{episode}") suspend fun deleteEpisodeRating(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Path("seriesId") seriesId: Int, @Path("season") season: Int, @Path("episode") episode: Int,
    ): MutationResult
    @GET("v2/account/recommendations/{mediaType}") suspend fun accountRecommendations(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
        @Path("mediaType") mediaType: String, @Query("page") page: Int, @Query("language") language: String,
    ): PagedResult<TitleSummary>
    @GET("v2/account/lists") suspend fun lists(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String, @Query("page") page: Int,
    ): PagedResult<UserList>
    @GET("v2/account/lists/{id}") suspend fun list(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
        @Path("id") id: Int, @Query("page") page: Int, @Query("language") language: String,
    ): UserList
    @POST("v2/account/lists") suspend fun createList(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String, @Body body: ListMetadata,
    ): MutationResult
    @PUT("v2/account/lists/{id}") suspend fun updateList(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
        @Path("id") id: Int, @Body body: ListMetadata,
    ): MutationResult
    @DELETE("v2/account/lists/{id}") suspend fun deleteList(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
        @Header("Idempotency-Key") idempotencyKey: String, @Path("id") id: Int,
    ): MutationResult
    @POST("v2/account/lists/{id}/items") suspend fun addListItems(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
        @Path("id") id: Int, @Body body: ListItemsMutation,
    ): MutationResult
    @HTTP(method = "DELETE", path = "v2/account/lists/{id}/items", hasBody = true) suspend fun removeListItems(
        @Header(CLIENT_HEADER) clientId: String, @Header("Authorization") authorization: String,
        @Path("id") id: Int, @Body body: ListItemsMutation,
    ): MutationResult

    companion object { const val CLIENT_HEADER = "X-SmartMovie-Client" }
}

@Serializable internal data class AuthAttemptRequest(@SerialName("return_uri") val returnUri: String, val mode: String)
@Serializable internal data class CompleteAuthRequest(@SerialName("attempt_id") val attemptId: String, @SerialName("device_code") val deviceCode: String?)
@Serializable internal data class AuthAttemptStatus(val status: String)
@Serializable internal data class LogoutResult(val success: Boolean)
@Serializable internal data class LibraryMutation(
    @SerialName("media_id") val mediaId: Int,
    val enabled: Boolean,
    @SerialName("mutation_id") val mutationId: String,
)
@Serializable internal data class RatingMutation(val value: Double, @SerialName("mutation_id") val mutationId: String)
@Serializable internal data class ListMetadata(
    val name: String,
    val description: String,
    val public: Boolean,
    @SerialName("iso_3166_1") val region: String? = null,
    @SerialName("iso_639_1") val language: String? = null,
    @SerialName("mutation_id") val mutationId: String,
)
@Serializable internal data class ListItemPayload(
    @SerialName("media_type") val mediaType: String,
    @SerialName("media_id") val mediaId: Int,
    val comment: String? = null,
)
@Serializable internal data class ListItemsMutation(
    val items: List<ListItemPayload>,
    @SerialName("mutation_id") val mutationId: String,
)

@Serializable
internal data class GenreEnvelope(val genres: List<Genre>)

@Serializable
internal data class ErrorEnvelope(val error: ErrorBody)

@Serializable
internal data class ErrorBody(
    val code: String,
    val message: String,
    @SerialName("request_id") val requestId: String? = null,
    @SerialName("retry_after") val retryAfter: Long? = null,
)
