package xyz.oddforge.reactionracer

data class GameState(
    val phase: GamePhase = GamePhase.Waiting,
    val delayMillis: Long = 3000L,
    val reactStartTime: Long = 0L,
    val reactionTime: Long = 0L,
    val bestTime: Long = Long.MAX_VALUE
)
