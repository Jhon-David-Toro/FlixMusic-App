package com.flix.music.feature.player

import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.Track
import com.flix.music.core.player.PlaybackStatus
import com.flix.music.core.player.PlayerState
import com.flix.music.testing.FakeFavoritesRepository
import com.flix.music.testing.FakePlayerController
import com.flix.music.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fadeToBlack = Track("1", "Fade To Black", listOf(ArtistRef("m", "Metallica")), null, null, null)
    private val favoritesRepository = FakeFavoritesRepository()
    private val playerController = FakePlayerController().apply {
        setState(PlayerState(status = PlaybackStatus.Playing, current = fadeToBlack))
    }
    private val viewModel by lazy { PlayerViewModel(playerController, favoritesRepository) }

    @Test
    fun `el corazón añade y quita la canción que suena de favoritos`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.isCurrentFavorite.collect {} }
        advanceUntilIdle()
        assertEquals(false, viewModel.isCurrentFavorite.value)

        viewModel.onToggleFavorite()
        advanceUntilIdle()
        assertEquals(true, viewModel.isCurrentFavorite.value)
        assertEquals(listOf(fadeToBlack), favoritesRepository.current.map { it.track })

        viewModel.onToggleFavorite()
        advanceUntilIdle()
        assertEquals(false, viewModel.isCurrentFavorite.value)
    }
}
