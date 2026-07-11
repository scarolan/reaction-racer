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
}
