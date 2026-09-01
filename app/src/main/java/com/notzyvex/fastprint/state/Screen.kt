package com.notzyvex.fastprint.state

enum class Screen { LAUNCH, SIGNIN, WELCOME, HOME, CUSTOMIZE, PRINTERS, PRINTING, DONE, FAILED, SETTINGS, HISTORY }

/** Timings carried over from the prototype. */
object Timing {
    const val LAUNCH_MS = 2000L
    const val WELCOME_MS = 2600L
}
