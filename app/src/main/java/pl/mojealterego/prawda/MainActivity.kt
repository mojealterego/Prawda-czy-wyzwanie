package pl.mojealterego.prawda

import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF07050A)
private val Panel = Color(0xFF180C16)
private val Gold = Color(0xFFE4C36F)
private val Muted = Color(0xFFC7B8C3)
private val Cream = Color(0xFFFFF4E3)
private val RoyalRed = Color(0xFF7A1028)
private val DeepRed = Color(0xFF26040D)
private val Purple = Color(0xFF321052)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PrawdaApp() }
    }
}

@Composable
private fun PrawdaApp() {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var state by remember(context) { mutableStateOf(GamePersistence.load(context) ?: GameState(players = listOf(Player("Gracz 1"), Player("Gracz 2")), customPrompts = GamePersistence.loadCustomPrompts(context))) }
    var started by remember { mutableStateOf(GamePersistence.load(context) != null) }
    val names = remember { mutableStateListOf("Gracz 1", "Gracz 2") }
    var newName by remember { mutableStateOf("") }
    var safetyVisible by remember { mutableStateOf(false) }
    var editorVisible by remember { mutableStateOf(false) }
    var summaryVisible by remember { mutableStateOf(false) }
    var panicVisible by remember { mutableStateOf(false) }
    var adultConfirmed by remember { mutableStateOf(false) }

    LaunchedEffect(state, started) { GamePersistence.save(context, state, started) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Ink, contentColor = Cream) {
            if (!adultConfirmed) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(28.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("EXPERIENCE OF ROYAL TRUTH", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(18.dp))
                    Text("ROYAL 18+", color = Cream, fontSize = 42.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    Text("Gra dla pełnoletnich. Zawiera intymne pytania i wyzwania dla par. Każde zadanie jest dobrowolne i można je pominąć.", color = Muted, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
                    Spacer(Modifier.height(28.dp))
                    GoldButton("MAM 18 LAT LUB WIĘCEJ", Modifier.fillMaxWidth()) { adultConfirmed = true }
                }
            } else if (!started) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("EXPERIENCE OF ROYAL TRUTH", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Text("Royal Truth\n& Dare", color = Cream, fontSize = 38.sp, lineHeight = 42.sp, fontWeight = FontWeight.SemiBold)
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
                        state = GameState(players = names.map { Player(it) }, customPrompts = state.customPrompts)
                        started = true
                    }
                }
            } else {
                GameScreen(
                    state = state,
                    onState = { state = it },
                    onSafety = { safetyVisible = true },
                    onEdit = { editorVisible = true },
                    onPanic = { panicVisible = true },
                    onFinish = { summaryVisible = true },
                    onExit = { started = false }
                )
                if (panicVisible) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { panicVisible = false },
                        containerColor = Color(0xFF24030A),
                        title = { Text("SAFE WORD", color = Color(0xFFFF718F), fontWeight = FontWeight.Bold) },
                        text = { Text("Gra została zatrzymana. Nie ma punktów ujemnych ani kary. Wznówcie dopiero wtedy, gdy wszyscy uczestnicy wyrażą zgodę.", color = Cream) },
                        confirmButton = { GoldButton("WRÓĆ DO GRY") { panicVisible = false } },
                        dismissButton = { TextButton(onClick = { panicVisible = false; started = false }) { Text("ZAKOŃCZ SESJĘ", color = Muted) } }
                    )
                }
                if (summaryVisible) {
                    val ranking = GameEngine.leaderboard(state)
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { summaryVisible = false },
                        containerColor = Panel,
                        title = { Text("FINAŁ SESJI", color = Gold, fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Rundy: ${state.round}  •  wykonane: ${state.totalCompleted}  •  pominięte: ${state.totalSkipped}", color = Muted)
                                ranking.forEachIndexed { index, player ->
                                    Text("${index + 1}. ${player.name}   ${player.score} pkt   ✓${player.completed}", color = if (index == 0) Gold else Cream, fontSize = if (index == 0) 18.sp else 15.sp, fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal)
                                }
                                Text("Najlepsza seria: ${state.bestStreak}", color = Muted)
                            }
                        },
                        confirmButton = { GoldButton("NOWA SESJA") { state = GameEngine.restartSession(state); summaryVisible = false } },
                        dismissButton = { TextButton(onClick = { summaryVisible = false; started = false }) { Text("WYJDŹ", color = Muted) } }
                    )
                }
                if (editorVisible) {
                    PromptEditorDialog(existing = state.customPrompts, onDismiss = { editorVisible = false },
                        onDelete = { id -> state = state.copy(customPrompts = state.customPrompts.filterNot { it.id == id }) },
                        onSave = { prompt ->
                            val updated = state.customPrompts.toMutableList()
                            val index = updated.indexOfFirst { it.id == prompt.id }
                            if (index >= 0) updated[index] = prompt else updated.add(prompt)
                            state = state.copy(customPrompts = updated)
                            editorVisible = false
                        })
                }
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
    onEdit: () -> Unit,
    onPanic: () -> Unit,
    onFinish: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    var secondsLeft by remember(state.currentPrompt?.id, state.pace) { mutableStateOf(state.pace.seconds) }
    var autoSpeak by remember { mutableStateOf(false) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var dice by remember { mutableStateOf<Int?>(null) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }
    DisposableEffect(context) {
        lateinit var engine: TextToSpeech
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) engine.language = Locale("pl", "PL")
        }
        tts = engine
        onDispose { engine.stop(); engine.shutdown() }
    }
    LaunchedEffect(state.currentPrompt?.id, state.pace, autoSpeak) {
        secondsLeft = state.pace.seconds
        val active = state.currentPrompt
        if (autoSpeak && active != null) tts?.speak(active.text, TextToSpeech.QUEUE_FLUSH, null, active.id)
        while (active != null && secondsLeft > 0) {
            kotlinx.coroutines.delay(1000)
            secondsLeft--
        }
    }
    val modeBrush = when (state.mode) {
        GameMode.RED_ROOM -> Brush.verticalGradient(listOf(DeepRed, Ink, Color.Black))
        GameMode.ROYAL -> Brush.verticalGradient(listOf(Color(0xFF201706), Ink, Color.Black))
        GameMode.CHAOS -> Brush.verticalGradient(listOf(Purple, Ink, Color.Black))
        GameMode.CLASSIC -> Brush.verticalGradient(listOf(Color(0xFF101018), Ink, Color.Black))
    }
    Column(
        modifier = Modifier.fillMaxSize().background(modeBrush).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("EXPERIENCE OF ROYAL TRUTH", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("Runda ${state.round + 1}  •  seria ${state.streak}  •  rekord ${state.bestStreak}", color = Muted, fontSize = 13.sp)
                if (targetIndex != null) Text("CEL: ${state.players.getOrNull(targetIndex!!)?.name ?: "—"}", color = if (state.mode == GameMode.RED_ROOM) Color(0xFFFF718F) else Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onEdit) { Text("Edytor", color = Gold) }
            TextButton(onClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onPanic() }) { Text("SAFE", color = Color(0xFFFF718F), fontWeight = FontWeight.Bold) }
            TextButton(onClick = onSafety) { Text("Zasady", color = Gold) }
            TextButton(onClick = onFinish) { Text("Finał", color = Gold) }
            TextButton(onClick = onExit) { Text("Wyjdź", color = Muted) }
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = Panel.copy(alpha = 0.88f)),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.border(1.dp, if (state.mode == GameMode.RED_ROOM) RoyalRed else Gold.copy(alpha = .28f), RoundedCornerShape(22.dp))
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("TERAZ GRA", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text(state.currentPlayer?.name ?: "—", color = Gold, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameMode.entries.forEach { mode ->
                        ChoicePill(when(mode) { GameMode.CLASSIC -> "Classic"; GameMode.CHAOS -> "Chaos"; GameMode.ROYAL -> "Royal"; GameMode.RED_ROOM -> "Red Room" }, state.mode == mode) {
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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChallengePace.entries.forEach { pace ->
                        ChoicePill(pace.label, state.pace == pace) { onState(state.copy(pace = pace)) }
                    }
                }
            }
        }
        AnimatedContent(
            targetState = state.currentPrompt,
            transitionSpec = { (fadeIn() + scaleIn(initialScale = .94f)) togetherWith (fadeOut() + scaleOut(targetScale = 1.04f)) },
            label = "prompt"
        ) { prompt ->
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors(containerColor = if (state.mode == GameMode.RED_ROOM) Color(0xFF330713) else Color(0xFF2A101F)),
                shape = RoundedCornerShape(30.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
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
                            Text("+${prompt.points} pkt  •  ${secondsLeft}s", color = Muted, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { tts?.speak(prompt.text, TextToSpeech.QUEUE_FLUSH, null, prompt.id) }) { Text("CZYTAJ", color = Gold) }
                                TextButton(onClick = { autoSpeak = !autoSpeak }) { Text(if (autoSpeak) "AUTO ✓" else "AUTO", color = if (autoSpeak) Gold else Muted) }
                            }
                        }
                    }
                }
            }
        }
        if (state.currentPrompt == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldButton("PRAWDA", Modifier.weight(1f)) { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onState(GameEngine.draw(state, PromptKind.TRUTH)) }
                GoldButton("WYZWANIE", Modifier.weight(1f)) { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onState(GameEngine.draw(state, PromptKind.DARE)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { dice = GameEngine.rollDice() }, modifier = Modifier.weight(1f)) { Text("🎲  KOŚĆ" + (dice?.let { "  $it" } ?: ""), color = Gold) }
                TextButton(onClick = { targetIndex = GameEngine.pickTarget(state) }, modifier = Modifier.weight(1f)) { Text("♛  RULETKA CELU", color = Gold) }
            }
            OutlinedAction("KOŁO DECYZJI") {
                val kind = if (kotlin.random.Random.nextBoolean()) PromptKind.TRUTH else PromptKind.DARE
                onState(GameEngine.draw(state, kind))
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedAction("POMIŃ") { onState(GameEngine.skip(state)); targetIndex = null; dice = null }
                GoldButton("WYKONANO  +${state.currentPrompt?.points ?: 0}", Modifier.weight(1.5f)) {
                    val points = state.currentPrompt?.points ?: 0
                    onState(GameEngine.nextPlayer(GameEngine.award(state, points)))
                    targetIndex = null
                    dice = null
                }
            }
            OutlinedAction("NASTĘPNY GRACZ") { onState(GameEngine.nextPlayer(state)) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("PUNKTY", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.width(10.dp))
            Text(state.players.sortedByDescending { it.score }.joinToString("   ") { "${it.name}: ${it.score}" }, color = Muted, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text("✓${state.totalCompleted}  ↷${state.totalSkipped}", color = Muted, fontSize = 11.sp)
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

@Composable
private fun PromptEditorDialog(existing: List<Prompt>, onDismiss: () -> Unit, onSave: (Prompt) -> Unit, onDelete: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(PromptKind.TRUTH) }
    var intensity by remember { mutableStateOf(Intensity.MEDIUM) }
    var points by remember { mutableStateOf("1") }
    var editingId by remember { mutableStateOf<String?>(null) }
    fun clearEditor() { text = ""; kind = PromptKind.TRUTH; intensity = Intensity.MEDIUM; points = "1"; editingId = null }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss, containerColor = Panel,
        title = { Text(if (editingId == null) "Własne karty" else "Edytuj kartę", color = Gold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (existing.isNotEmpty()) {
                    Text("TALIA (${existing.size})", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    LazyColumn(modifier = Modifier.height(150.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        itemsIndexed(existing, key = { _, prompt -> prompt.id }) { _, prompt ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prompt.text, color = Cream, fontSize = 12.sp, maxLines = 2)
                                    Text("${if (prompt.kind == PromptKind.TRUTH) "Prawda" else "Wyzwanie"} · ${prompt.intensity.label} · ${prompt.points} pkt", color = Muted, fontSize = 10.sp)
                                }
                                TextButton(onClick = { editingId = prompt.id; text = prompt.text; kind = prompt.kind; intensity = prompt.intensity; points = prompt.points.toString() }) { Text("Edytuj", color = Gold, fontSize = 11.sp) }
                                TextButton(onClick = { onDelete(prompt.id); if (editingId == prompt.id) clearEditor() }) { Text("Usuń", color = Muted, fontSize = 11.sp) }
                            }
                        }
                    }
                }
                Text(if (editingId == null) "DODAJ KARTĘ" else "TREŚĆ KARTY", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                OutlinedTextField(value = text, onValueChange = { text = it.take(240) }, label = { Text("Treść pytania lub zadania") }, minLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ChoicePill("Prawda", kind == PromptKind.TRUTH) { kind = PromptKind.TRUTH }; ChoicePill("Wyzwanie", kind == PromptKind.DARE) { kind = PromptKind.DARE } }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Intensity.entries.forEach { item -> ChoicePill(item.label, intensity == item) { intensity = item } } }
                OutlinedTextField(value = points, onValueChange = { value -> points = value.filter(Char::isDigit).take(2) }, label = { Text("Punkty (1–10)") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { val clean = text.trim(); val score = points.toIntOrNull()?.coerceIn(1, 10) ?: 1; if (clean.isNotEmpty()) onSave(Prompt(editingId ?: ("custom_" + System.currentTimeMillis()), clean, kind, intensity, score)) }, enabled = text.isNotBlank()) { Text(if (editingId == null) "Dodaj kartę" else "Zapisz zmiany", color = Gold) } },
        dismissButton = { Row { if (editingId != null) TextButton(onClick = { clearEditor() }) { Text("Nowa", color = Gold) }; TextButton(onClick = onDismiss) { Text("Zamknij", color = Muted) } } }
    )
}
