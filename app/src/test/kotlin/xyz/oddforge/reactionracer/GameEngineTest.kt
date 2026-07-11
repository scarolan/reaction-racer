package xyz.oddforge.reactionracer

import org.junit.Assert.assertEquals
import org.junit.Test

class GameEngineTest {

    @Test
    fun formatReactionTime_displaysMilliseconds() {
        assertEquals("250ms", GameEngine.formatReactionTime(250))
    }

    @Test
    fun formatReactionTime_zero() {
        assertEquals("0ms", GameEngine.formatReactionTime(0))
    }

    // --- createInitialState ---

    @Test
    fun createInitialState_setsWaitingPhaseAndDelay() {
        val state = GameEngine.createInitialState(3500L)
        assertEquals(GamePhase.Waiting, state.phase)
        assertEquals(3500L, state.delayMillis)
        assertEquals(0L, state.reactStartTime)
        assertEquals(0L, state.reactionTime)
        assertEquals(Long.MAX_VALUE, state.bestTime)
    }

    // --- StartPressed ---

    @Test
    fun startPressed_fromWaiting_transitionsToCountdown() {
        val state = GameEngine.createInitialState(3000L)
        val result = GameEngine.reduce(state, GameEvent.StartPressed, 1000L)
        assertEquals(GamePhase.Countdown, result.phase)
        assertEquals(3000L, result.delayMillis)
    }

    @Test
    fun startPressed_fromCountdown_ignored() {
        val state = GameEngine.createInitialState(3000L).copy(phase = GamePhase.Countdown)
        val result = GameEngine.reduce(state, GameEvent.StartPressed, 1000L)
        assertEquals(state, result)
    }

    @Test
    fun startPressed_fromReact_ignored() {
        val state = GameEngine.createInitialState(3000L).copy(phase = GamePhase.React)
        val result = GameEngine.reduce(state, GameEvent.StartPressed, 1000L)
        assertEquals(state, result)
    }

    @Test
    fun startPressed_fromResult_ignored() {
        val state = GameEngine.createInitialState(3000L).copy(phase = GamePhase.Result)
        val result = GameEngine.reduce(state, GameEvent.StartPressed, 1000L)
        assertEquals(state, result)
    }

    @Test
    fun startPressed_fromTooEarly_ignored() {
        val state = GameEngine.createInitialState(3000L).copy(phase = GamePhase.TooEarly)
        val result = GameEngine.reduce(state, GameEvent.StartPressed, 1000L)
        assertEquals(state, result)
    }

    // --- DelayComplete ---

    @Test
    fun delayComplete_fromCountdown_transitionsToReact() {
        val state = GameEngine.createInitialState(3000L).copy(phase = GamePhase.Countdown)
        val result = GameEngine.reduce(state, GameEvent.DelayComplete, 5000L)
        assertEquals(GamePhase.React, result.phase)
        assertEquals(5000L, result.reactStartTime)
    }

    @Test
    fun delayComplete_fromWaiting_ignored() {
        val state = GameEngine.createInitialState(3000L)
        val result = GameEngine.reduce(state, GameEvent.DelayComplete, 5000L)
        assertEquals(state, result)
    }

    @Test
    fun delayComplete_fromReact_ignored() {
        val state = GameEngine.createInitialState(3000L).copy(phase = GamePhase.React)
        val result = GameEngine.reduce(state, GameEvent.DelayComplete, 5000L)
        assertEquals(state, result)
    }

    // --- ScreenTapped during Countdown (too early) ---

    @Test
    fun screenTapped_duringCountdown_transitionsToTooEarly() {
        val state = GameEngine.createInitialState(3000L).copy(phase = GamePhase.Countdown)
        val result = GameEngine.reduce(state, GameEvent.ScreenTapped, 1500L)
        assertEquals(GamePhase.TooEarly, result.phase)
    }

    // --- ScreenTapped during React ---

    @Test
    fun screenTapped_duringReact_transitionsToResult() {
        val state = GameEngine.createInitialState(3000L).copy(phase = GamePhase.React, reactStartTime = 5000L)
        val result = GameEngine.reduce(state, GameEvent.ScreenTapped, 5250L)
        assertEquals(GamePhase.Result, result.phase)
        assertEquals(250L, result.reactionTime)
    }

    @Test
    fun screenTapped_duringReact_updatesBestTime() {
        val state = GameEngine.createInitialState(3000L).copy(
            phase = GamePhase.React,
            reactStartTime = 1000L,
            bestTime = Long.MAX_VALUE
        )
        val result = GameEngine.reduce(state, GameEvent.ScreenTapped, 1300L)
        assertEquals(300L, result.reactionTime)
        assertEquals(300L, result.bestTime)
    }

    @Test
    fun screenTapped_duringReact_preservesBetterBestTime() {
        val state = GameEngine.createInitialState(3000L).copy(
            phase = GamePhase.React,
            reactStartTime = 1000L,
            bestTime = 200L
        )
        val result = GameEngine.reduce(state, GameEvent.ScreenTapped, 1500L)
        assertEquals(500L, result.reactionTime)
        assertEquals(200L, result.bestTime)
    }

    @Test
    fun screenTapped_duringReact_replacesBestTimeWhenFaster() {
        val state = GameEngine.createInitialState(3000L).copy(
            phase = GamePhase.React,
            reactStartTime = 1000L,
            bestTime = 400L
        )
        val result = GameEngine.reduce(state, GameEvent.ScreenTapped, 1150L)
        assertEquals(150L, result.reactionTime)
        assertEquals(150L, result.bestTime)
    }

    // --- ScreenTapped ignored in wrong phases ---

    @Test
    fun screenTapped_duringWaiting_ignored() {
        val state = GameEngine.createInitialState(3000L)
        val result = GameEngine.reduce(state, GameEvent.ScreenTapped, 1000L)
        assertEquals(state, result)
    }

    @Test
    fun screenTapped_duringResult_ignored() {
        val state = GameEngine.createInitialState(3000L).copy(phase = GamePhase.Result, reactionTime = 250L)
        val result = GameEngine.reduce(state, GameEvent.ScreenTapped, 1000L)
        assertEquals(state, result)
    }

    @Test
    fun screenTapped_duringTooEarly_ignored() {
        val state = GameEngine.createInitialState(3000L).copy(phase = GamePhase.TooEarly)
        val result = GameEngine.reduce(state, GameEvent.ScreenTapped, 1000L)
        assertEquals(state, result)
    }

    // --- TryAgain ---

    @Test
    fun tryAgain_fromResult_resetsToWaitingWithNewDelay() {
        val state = GameEngine.createInitialState(3000L).copy(
            phase = GamePhase.Result,
            reactionTime = 250L,
            bestTime = 250L
        )
        val result = GameEngine.reduce(state, GameEvent.TryAgain(4000L), 9000L)
        assertEquals(GamePhase.Waiting, result.phase)
        assertEquals(4000L, result.delayMillis)
        assertEquals(0L, result.reactionTime)
        assertEquals(0L, result.reactStartTime)
    }

    @Test
    fun tryAgain_preservesBestTime() {
        val state = GameEngine.createInitialState(3000L).copy(
            phase = GamePhase.Result,
            bestTime = 180L
        )
        val result = GameEngine.reduce(state, GameEvent.TryAgain(2500L), 9000L)
        assertEquals(180L, result.bestTime)
    }

    @Test
    fun tryAgain_fromTooEarly_resetsToWaiting() {
        val state = GameEngine.createInitialState(3000L).copy(
            phase = GamePhase.TooEarly,
            bestTime = 300L
        )
        val result = GameEngine.reduce(state, GameEvent.TryAgain(4500L), 9000L)
        assertEquals(GamePhase.Waiting, result.phase)
        assertEquals(4500L, result.delayMillis)
        assertEquals(300L, result.bestTime)
    }

    // --- Purity ---

    @Test
    fun reduce_doesNotMutateInputState() {
        val original = GameEngine.createInitialState(3000L)
        GameEngine.reduce(original, GameEvent.StartPressed, 1000L)
        assertEquals(GamePhase.Waiting, original.phase)
        assertEquals(0L, original.reactStartTime)
    }
}
