package com.flix.music.core.player

import androidx.media3.common.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackStatusMappingTest {

    private fun status(
        hasMedia: Boolean = true,
        playbackState: Int = Player.STATE_READY,
        playWhenReady: Boolean = true,
        hasError: Boolean = false,
        currentTrackWasReady: Boolean = true,
    ) = playbackStatusOf(hasMedia, playbackState, playWhenReady, hasError, currentTrackWasReady)

    @Test
    fun `sin cola el reproductor está en reposo`() {
        assertEquals(PlaybackStatus.Idle, status(hasMedia = false, playbackState = Player.STATE_IDLE))
    }

    @Test
    fun `listo y con intención de sonar es reproduciendo, sin ella es pausa`() {
        assertEquals(PlaybackStatus.Playing, status())
        assertEquals(PlaybackStatus.Paused, status(playWhenReady = false))
    }

    @Test
    fun `esperar antes de que la pista haya sonado es cargando, después es buffering`() {
        assertEquals(
            PlaybackStatus.Loading,
            status(playbackState = Player.STATE_BUFFERING, currentTrackWasReady = false),
        )
        assertEquals(
            PlaybackStatus.Buffering,
            status(playbackState = Player.STATE_BUFFERING, currentTrackWasReady = true),
        )
    }

    @Test
    fun `el error tiene prioridad y el final de la cola es terminado`() {
        assertEquals(PlaybackStatus.Error, status(hasError = true))
        assertEquals(PlaybackStatus.Ended, status(playbackState = Player.STATE_ENDED))
    }
}
