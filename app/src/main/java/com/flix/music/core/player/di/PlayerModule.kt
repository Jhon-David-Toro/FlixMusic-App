package com.flix.music.core.player.di

import com.flix.music.core.player.MediaPlayerController
import com.flix.music.core.player.PlayerController
import com.flix.music.core.player.stream.NewPipeStreamResolver
import com.flix.music.core.player.stream.StreamResolver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindPlayerController(impl: MediaPlayerController): PlayerController

    @Binds
    @Singleton
    abstract fun bindStreamResolver(impl: NewPipeStreamResolver): StreamResolver
}
