package com.flix.music

import android.app.Application
import com.flix.music.core.data.youtube.NewPipeSetup
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Warms NewPipe up at launch, so the first song does not pay for YouTube's player script. */
@HiltAndroidApp
class FlixApplication : Application() {

    @Inject
    lateinit var newPipeSetup: NewPipeSetup

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { newPipeSetup.warmUp() }
    }
}
