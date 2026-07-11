package xyz.oddforge.reactionracer

object GameEngine {

    fun formatReactionTime(millis: Long): String {
        return "${millis}ms"
    }

    fun createInitialState(randomDelay: Long): GameState = GameState(delayMillis = randomDelay)

    fun reduce(state: GameState, event: GameEvent, currentTimeMillis: Long): GameState = when (event) {
        is GameEvent.StartPressed -> when (state.phase) {
            GamePhase.Waiting -> state.copy(phase = GamePhase.Countdown)
            else -> state
        }
        is GameEvent.DelayComplete -> when (state.phase) {
            GamePhase.Countdown -> state.copy(phase = GamePhase.React, reactStartTime = currentTimeMillis)
            else -> state
        }
        is GameEvent.ScreenTapped -> when (state.phase) {
            GamePhase.Countdown -> state.copy(phase = GamePhase.TooEarly)
            GamePhase.React -> {
                val reaction = currentTimeMillis - state.reactStartTime
                val best = if (reaction < state.bestTime) reaction else state.bestTime
                state.copy(phase = GamePhase.Result, reactionTime = reaction, bestTime = best)
            }
            else -> state
        }
        is GameEvent.TryAgain -> state.copy(
            phase = GamePhase.Waiting,
            delayMillis = event.newDelayMillis,
            reactStartTime = 0L,
            reactionTime = 0L
        )
    }
}
