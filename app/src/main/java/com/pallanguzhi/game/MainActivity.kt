package com.pallanguzhi.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class BoardStyle { RECTANGULAR, FISH }

enum class Player { TOP, BOTTOM }

data class GameState(
    val pits: MutableList<Int>,
    var topCaptured: Int = 0,
    var bottomCaptured: Int = 0,
    var currentPlayer: Player = Player.BOTTOM,
    var message: String = "Bottom player turn"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PallankuzhiScreen()
                }
            }
        }
    }
}

@Composable
fun PallankuzhiScreen() {
    var boardStyle by remember { mutableStateOf(BoardStyle.RECTANGULAR) }
    val pits = remember { mutableStateListOf<Int>().apply { addAll(initialPits()) } }
    var topCaptured by remember { mutableIntStateOf(0) }
    var bottomCaptured by remember { mutableIntStateOf(0) }
    var currentPlayer by remember { mutableStateOf(Player.BOTTOM) }
    var message by remember { mutableStateOf("Bottom player turn") }

    fun resetGame() {
        pits.clear()
        pits.addAll(initialPits())
        topCaptured = 0
        bottomCaptured = 0
        currentPlayer = Player.BOTTOM
        message = "Bottom player turn"
    }

    fun applyMove(startPit: Int) {
        if (!isPlayablePit(startPit, currentPlayer) || pits[startPit] == 0) {
            message = "Choose one of your non-empty cups."
            return
        }

        var seedsInHand = pits[startPit]
        pits[startPit] = 0
        var index = startPit

        while (true) {
            while (seedsInHand > 0) {
                index = (index + 1) % pits.size
                pits[index] = pits[index] + 1
                seedsInHand--
            }

            val landedCount = pits[index]
            if (landedCount > 1) {
                seedsInHand = landedCount
                pits[index] = 0
                continue
            }

            if (landedCount == 1 && isPlayablePit(index, currentPlayer)) {
                val opposite = oppositePit(index)
                val capture = pits[opposite]
                if (capture > 0) {
                    pits[index] = 0
                    pits[opposite] = 0
                    val capturedTotal = capture + 1
                    if (currentPlayer == Player.BOTTOM) bottomCaptured += capturedTotal else topCaptured += capturedTotal
                    message = "${currentPlayer.name.lowercase().replaceFirstChar { it.uppercase() }} captured $capturedTotal shells!"
                }
            }

            break
        }

        if (isGameOver(pits)) {
            val topRemaining = (0..6).sumOf { pits[it] }
            val bottomRemaining = (7..13).sumOf { pits[it] }
            repeat(7) {
                pits[it] = 0
                pits[it + 7] = 0
            }
            topCaptured += topRemaining
            bottomCaptured += bottomRemaining

            message = when {
                topCaptured > bottomCaptured -> "Top player wins!"
                bottomCaptured > topCaptured -> "Bottom player wins!"
                else -> "It is a draw!"
            }
        } else {
            currentPlayer = if (currentPlayer == Player.BOTTOM) Player.TOP else Player.BOTTOM
            message += " ${currentPlayer.name.lowercase().replaceFirstChar { it.uppercase() }} player turn."
        }
    }

    val boardShape = if (boardStyle == BoardStyle.RECTANGULAR) RoundedCornerShape(28.dp) else RoundedCornerShape(50)
    val woodColor = Color(0xFF9A643D)
    val pitColor = Color(0xFF5A3418)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5E6CF))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Pallankuzhi", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Traditional Tamil game • 14 cups • 148 shells")
        Text(message)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { boardStyle = BoardStyle.RECTANGULAR }) { Text("Rectangular") }
            Button(onClick = { boardStyle = BoardStyle.FISH }) { Text("Fish") }
            Button(onClick = { resetGame() }) { Text("Reset") }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = woodColor),
            shape = boardShape
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PitRow(indices = (0..6).toList().reversed(), pits = pits, pitColor = pitColor, onPitTap = ::applyMove)
                PitRow(indices = (7..13).toList(), pits = pits, pitColor = pitColor, onPitTap = ::applyMove)
            }
        }

        Text("Top captured: $topCaptured")
        Text("Bottom captured: $bottomCaptured")

        Text(
            "Rules in this app: relay sowing continues when the final shell lands in a non-empty cup; capture happens when the final shell lands in your own empty cup opposite a non-empty rival cup.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun PitRow(indices: List<Int>, pits: List<Int>, pitColor: Color, onPitTap: (Int) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(indices) { _, pitIndex ->
            val count = pits[pitIndex]
            Button(
                onClick = { onPitTap(pitIndex) },
                modifier = Modifier.width(72.dp).height(72.dp),
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(pitColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = count.toString(), color = Color(0xFFFFE4C4), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun initialPits(): List<Int> {
    return List(14) { index -> if (index < 8) 11 else 10 }
}

private fun isPlayablePit(index: Int, player: Player): Boolean {
    return when (player) {
        Player.TOP -> index in 0..6
        Player.BOTTOM -> index in 7..13
    }
}

private fun oppositePit(index: Int): Int = 13 - index

private fun isGameOver(pits: List<Int>): Boolean {
    val topEmpty = (0..6).all { pits[it] == 0 }
    val bottomEmpty = (7..13).all { pits[it] == 0 }
    return topEmpty || bottomEmpty
}
