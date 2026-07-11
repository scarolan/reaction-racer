package xyz.oddforge.reactionracer

sealed class GameEvent {
    data object StartPressed : GameEvent()
    data object DelayComplete : GameEvent()
    data object ScreenTapped : GameEvent()
    data class TryAgain(val newDelayMillis: Long) : GameEvent()
}
