package com.flix.music.feature.search

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.flix.music.R

/** Mood chips from the design ("Según tu ánimo"). Each one runs a curated search [query]. */
enum class SearchMood(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    val query: String,
) {
    Energy(R.string.mood_energy, R.drawable.ic_bolt, "música energética"),
    Night(R.string.mood_night, R.drawable.ic_dark_mode, "música para la noche"),
    Focus(R.string.mood_focus, R.drawable.ic_psychology, "música para concentrarse"),
    Workout(R.string.mood_workout, R.drawable.ic_directions_run, "música para entrenar"),
}
