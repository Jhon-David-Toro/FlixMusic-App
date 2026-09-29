package com.flix.music.core.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FlixApi {
    @GET("health")
    suspend fun health(): HealthDto

    @GET("api/music/search")
    suspend fun search(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20
    ): SearchResponseDto

    @GET("api/music/songs/{songId}")
    suspend fun song(@Path("songId") songId: String): SongDetailDto

    /** Resolves and caches the audio URL on the server; the response body is not needed. */
    @GET("api/music/songs/{songId}/stream")
    suspend fun prefetchStream(@Path("songId") songId: String)
}
