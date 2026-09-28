package com.flix.music.feature.search

import androidx.annotation.StringRes
import com.flix.music.R

enum class SearchFilter(@StringRes val label: Int) {
    All(R.string.search_filter_all),
    Songs(R.string.search_section_songs),
    Artists(R.string.search_section_artists),
    Albums(R.string.search_section_albums),
}