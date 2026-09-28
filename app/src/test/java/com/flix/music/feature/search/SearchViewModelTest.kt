package com.flix.music.feature.search

import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.FlixError
import com.flix.music.core.model.Track
import com.flix.music.testing.FakeMusicRepository
import com.flix.music.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMusicRepository()
    private val viewModel = SearchViewModel(repository)

    private val fadeToBlack = Track("1", "Fade To Black", listOf(ArtistRef(null, "Metallica")), null, null, null)

    @Test
    fun `escribir rápido hace una sola búsqueda`() = runTest {
        repository.tracks = listOf(fadeToBlack)

        // Simula teclear letra a letra, sin pausa.
        viewModel.onQueryChange("fa")
        viewModel.onQueryChange("fad")
        viewModel.onQueryChange("fade")
        advanceTimeBy(299.milliseconds)
        assertEquals(0, repository.searchCount)
        advanceUntilIdle()
        assertEquals(1, repository.searchCount)
        assertEquals(
            // fadeToBlack tiene un artista sin id y no tiene álbum: los derivados salen vacíos.
            SearchUiState.Results(listOf(fadeToBlack), artists = emptyList(), albums = emptyList()),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `menos de dos letras no busca y queda en reposo`() = runTest {
        viewModel.onQueryChange("f")
        advanceUntilIdle()

        assertEquals(0, repository.searchCount)
        assertEquals(SearchUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `sin resultados muestra el estado vacío con la búsqueda`() = runTest {
        repository.tracks = emptyList()

        viewModel.onQueryChange("qwxzqwxz")
        advanceUntilIdle()

        assertEquals(SearchUiState.Empty("qwxzqwxz"), viewModel.uiState.value)
    }

    @Test
    fun `si el repository falla muestra el error`() = runTest {
        repository.error = FlixError.ServerUnreachable

        viewModel.onQueryChange("fade")
        advanceUntilIdle()

        assertEquals(SearchUiState.Error(FlixError.ServerUnreachable), viewModel.uiState.value)
    }

    @Test
    fun `reintentar después de un error muestra los resultados`() = runTest {
        repository.error = FlixError.ServerUnreachable
        viewModel.onQueryChange("fade")
        advanceUntilIdle()
        repository.error = null
        repository.tracks = listOf(fadeToBlack)
        viewModel.onRetry()
        advanceUntilIdle()

        assertEquals(
            // fadeToBlack tiene un artista sin id y no tiene álbum: los derivados salen vacíos.
            SearchUiState.Results(listOf(fadeToBlack), artists = emptyList(), albums = emptyList()),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `borrar vuelve al reposo`() = runTest {
        repository.tracks = listOf(fadeToBlack)
        viewModel.onQueryChange("fade")
        advanceUntilIdle()

        viewModel.onClear()

        assertEquals("", viewModel.query)
        assertEquals(SearchUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `mientras carga una búsqueda nueva, conserva los resultados anteriores`() = runTest {
        repository.tracks = listOf(fadeToBlack)
        viewModel.onQueryChange("fade")
        advanceUntilIdle()
        val firstResults = viewModel.uiState.value as SearchUiState.Results

        repository.responseDelayMillis = 1_000 // la siguiente respuesta tarda 1 s
        viewModel.onQueryChange("fade to")
        advanceTimeBy(301.milliseconds)         // pasa el debounce: ya está buscando

        assertEquals(SearchUiState.Loading(previous = firstResults), viewModel.uiState.value)
    }

    @Test
    fun `una búsqueda nueva vuelve al filtro Todo`() = runTest {
        viewModel.onFilterChange(SearchFilter.Artists)

        viewModel.onQueryChange("fade")

        assertEquals(SearchFilter.All, viewModel.filter)
    }
}
