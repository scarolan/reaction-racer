package xyz.oddforge.reactionracer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ReactionRacerGame()
            }
        }
    }
}

@Composable
private fun ReactionRacerGame() {
    val randomDelay = remember { { (2000L..5000L).random() } }
    var state by remember { mutableStateOf(GameEngine.createInitialState(randomDelay())) }

    val dispatch = { event: GameEvent ->
        state = GameEngine.reduce(state, event, System.currentTimeMillis())
    }

    LaunchedEffect(state.phase) {
        if (state.phase == GamePhase.Countdown) {
            delay(state.delayMillis)
            dispatch(GameEvent.DelayComplete)
        }
    }

    val backgroundColor = when (state.phase) {
        GamePhase.Waiting -> MaterialTheme.colorScheme.surface
        GamePhase.Countdown -> Color.Red
        GamePhase.React -> Color.Green
        GamePhase.Result -> MaterialTheme.colorScheme.surface
        GamePhase.TooEarly -> Color(0xFFFF9800)
    }

    val textColor = when (state.phase) {
        GamePhase.Countdown, GamePhase.React, GamePhase.TooEarly -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable {
                when (state.phase) {
                    GamePhase.Waiting -> dispatch(GameEvent.StartPressed)
                    GamePhase.Countdown -> dispatch(GameEvent.ScreenTapped)
                    GamePhase.React -> dispatch(GameEvent.ScreenTapped)
                    else -> {}
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (state.phase) {
                GamePhase.Waiting -> {
                    Text("Tap to start", fontSize = 28.sp, color = textColor)
                    if (state.bestTime != Long.MAX_VALUE) {
                        Text(
                            "Best: ${GameEngine.formatReactionTime(state.bestTime)}",
                            fontSize = 16.sp,
                            color = textColor,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                GamePhase.Countdown -> {
                    Text("Wait for green...", fontSize = 28.sp, color = textColor)
                }
                GamePhase.React -> {
                    Text("TAP!", fontSize = 48.sp, color = textColor)
                }
                GamePhase.Result -> {
                    Text(
                        "Your time: ${GameEngine.formatReactionTime(state.reactionTime)}",
                        fontSize = 28.sp,
                        color = textColor
                    )
                    if (state.bestTime != Long.MAX_VALUE) {
                        Text(
                            "Best: ${GameEngine.formatReactionTime(state.bestTime)}",
                            fontSize = 16.sp,
                            color = textColor,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Button(
                        onClick = { dispatch(GameEvent.TryAgain(randomDelay())) },
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Text("Try Again")
                    }
                }
                GamePhase.TooEarly -> {
                    Text("Too early!", fontSize = 28.sp, color = textColor)
                    Button(
                        onClick = { dispatch(GameEvent.TryAgain(randomDelay())) },
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}
