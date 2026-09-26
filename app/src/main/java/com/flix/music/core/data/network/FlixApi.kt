package com.flix.music.core.data.network

import retrofit2.http.GET

interface FlixApi {
    @GET("health")
    suspend fun health(): HealthDto
}