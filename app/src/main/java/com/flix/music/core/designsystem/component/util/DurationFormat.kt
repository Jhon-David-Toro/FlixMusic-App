package com.flix.music.core.designsystem.component.util

import kotlin.time.Duration

fun Duration.toClock(): String =
    "%d:%02d".format(inWholeMinutes, inWholeSeconds % 60)