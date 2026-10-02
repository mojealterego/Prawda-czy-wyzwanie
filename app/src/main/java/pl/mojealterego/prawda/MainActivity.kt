package pl.mojealterego.prawda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF090909)
private val Panel = Color(0xFF171614)
private val Gold = Color(0xFFD7B56D)
private val Muted = Color(0xFFB5B0A5)
private val Cream = Color(0xFFF4EEDF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PrawdaApp() }
    }
}

@Composable
private fun PrawdaApp() {
    var started by remember { mutableStateOf(false) }
    val names = remember { mutableStateListOf("Gracz 1", "Gracz 2") }
    var newName by remember { mutableStateOf("") }
    var state by remember { mutableStateOf(GameState(players = listOf(Player("Gracz 1"), Player("Gracz 2")))) }
    var safetyVisible by remember { mutableStateOf(false) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Ink, contentColor = Cream) {
            if (!started) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("ROYAL EDITION", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Text("Prawda czy\nWyzwanie", color = Cream, fontSize = 38.sp, lineHeight = 42.sp, fontWeight = FontWeight.SemiBold)
                    Text("Dodaj uczestników. Każdy może odmówić lub pominąć kartę bez tłumaczenia.", color = Muted, fontSize = 14.sp)
                    Text("GRACZE", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(names) { index, name ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(name, modifier = Modifier.weight(1f), color = Cream, fontSize = 16.sp)
                                TextButton(onClick = { if (names.size > 1) names.removeAt(index) }) { Text("Usuń", color = Muted) }
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newName, onValueChange = { newName = it }, modifier = Modifier.weight(1f),
                            label = { Text("Imię lub pseudonim") }, singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            val cleaned = newName.trim()
                            if (cleaned.isNotEmpty() && names.size < 12) names.add(cleaned)
                            newName = ""
                        }) { Text("Dodaj", color = Gold) }
                    }
                    GoldButton("ROZPOCZNIJ GRĘ", Modifier.fillMaxWidth()) {
                        state = GameState(players = names.map { Player(it) })
                        started = true
                    }
                }
            } else {
                GameScreen(
                    state = state,
                    onState = { state = it },
                    onSafety = { safetyVisible = true },
                    onExit = { started = false }
                )
                if (safetyVisible) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { safetyVisible = false },
                        containerColor = Panel,
                        title = { Text("Zasady bezpieczeństwa", color = Gold) },
                        text = { Text("Zgoda jest dobrowolna i może zostać wycofana w dowolnym momencie. Każdy może pominąć pytanie lub wyzwanie bez kary i bez podawania powodu. Nie wykonuj zadań niebezpiecznych, naruszających prywatność ani wymagających kontaktu bez zgody drugiej osoby.", color = Cream) },
                        confirmButton = { TextButton(onClick = { safetyVisible = false }) { Text("Rozumiem", color = Gold) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameScreen(
    state: GameState,
    onState: (GameState) -> Unit,
    onSafety: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("ROYAL EDITION", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("Runda ${state.round + 1}", color = Muted, fontSize = 13.sp)
            }
            TextButton(onClick = onSafety) { Text("Zasady", color = Gold) }
            TextButton(onClick = onExit) { Text("Wyjdź", color = Muted) }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("TERAZ GRA", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text(state.currentPlayer?.name ?: "—", color = Gold, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameMode.entries.forEach { mode ->
                        ChoicePill(mode.name.lowercase().replaceFirstChar { it.uppercase() }, state.mode == mode) {
                            onState(state.copy(mode = mode, currentPrompt = null))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Intensity.entries.forEach { intensity ->
                        ChoicePill(intensity.label, state.intensity == intensity) {
                            onState(state.copy(intensity = intensity, currentPrompt = null))
                        }
                    }
                }
            }
        }
        AnimatedContent(
            targetState = state.currentPrompt,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "prompt"
        ) { prompt ->
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF211E18)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    if (prompt == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("WYBIERZ KARTĘ", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Text("Prawda czy wyzwanie?", color = Cream, fontSize = 23.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                            Text("Nowa karta nie powtórzy się w tej sesji, dopóki dostępne są inne.", color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            Text(if (prompt.royal) "KARTA KRÓLEWSKA" else if (prompt.kind == PromptKind.TRUTH) "PRAWDA" else "WYZWANIE", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                            Text(prompt.text, color = Cream, fontSize = 24.sp, textAlign = TextAlign.Center, lineHeight = 32.sp)
                            Text("+${prompt.points} pkt za wykonanie", color = Muted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        if (state.currentPrompt == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldButton("PRAWDA", Modifier.weight(1f)) { onState(GameEngine.draw(state, PromptKind.TRUTH)) }
                GoldButton("WYZWANIE", Modifier.weight(1f)) { onState(GameEngine.draw(state, PromptKind.DARE)) }
            }
            OutlinedAction("KOŁO DECYZJI") {
                val kind = if (kotlin.random.Random.nextBoolean()) PromptKind.TRUTH else PromptKind.DARE
                onState(GameEngine.draw(state, kind))
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedAction("POMIŃ") { onState(GameEngine.skip(state)) }
                GoldButton("WYKONANO  +${state.currentPrompt?.points ?: 0}", Modifier.weight(1.5f)) {
                    val points = state.currentPrompt?.points ?: 0
                    onState(GameEngine.nextPlayer(GameEngine.award(state, points)))
                }
            }
            OutlinedAction("NASTĘPNY GRACZ") { onState(GameEngine.nextPlayer(state)) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("PUNKTY", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.width(10.dp))
            Text(state.players.joinToString("   ") { "${it.name}: ${it.score}" }, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ChoicePill(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = if (selected) Ink else Muted),
        modifier = Modifier.background(if (selected) Gold else Color(0xFF26231E), RoundedCornerShape(50))
    ) { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
}

@Composable
private fun GoldButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
        shape = RoundedCornerShape(14.dp)
    ) { Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.5.sp) }
}

@Composable
private fun OutlinedAction(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}
