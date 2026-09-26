package com.flix.music.core.data.network

import kotlinx.serialization.Serializable

@Serializable
data class HealthDto(
    val status: String
)