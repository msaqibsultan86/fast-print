package com.notzyvex.fastprint.state

enum class Screen {
    LAUNCH,
    UPDATE,
    SIGNIN,
    WELCOME,
    HOME,
    CUSTOMIZE,
    PRINTERS,
    PRINTING,
    DONE,
    FAILED,
    SETTINGS,
    HISTORY,
}

/** The five in-screen states of the update flow. */
enum class UpdateStage { AVAILABLE, PERMISSION, DOWNLOADING, INSTALLING, FAILED }

/** Timings carried over from the prototype. */
object Timing {
    const val LAUNCH_MS = 2000L
    const val WELCOME_MS = 2600L
    const val TOAST_MS = 2200L

    /** How long the splash will wait on the update check before moving on. */
    const val UPDATE_CHECK_BUDGET_MS = 2500L
}
