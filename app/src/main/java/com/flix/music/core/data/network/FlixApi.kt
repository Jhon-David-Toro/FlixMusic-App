package com.flix.music.core.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface FlixApi {
    @GET("health")
    suspend fun health(): HealthDto

    @GET("api/music/search")
    suspend fun search(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20
    ): SearchResponseDto
}