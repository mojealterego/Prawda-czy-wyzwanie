package pl.mojealterego.prawda

import kotlin.random.Random

enum class GameMode { CLASSIC, CHAOS, ROYAL }
enum class Intensity(val label: String) { EASY("Lekki"), MEDIUM("Średni"), BOLD("Odważny") }
enum class PromptKind { TRUTH, DARE }

data class Prompt(
    val id: String,
    val text: String,
    val kind: PromptKind,
    val intensity: Intensity,
    val points: Int = 1,
    val royal: Boolean = false
)

data class Player(val name: String, val score: Int = 0)

data class GameState(
    val players: List<Player>,
    val activePlayer: Int = 0,
    val mode: GameMode = GameMode.CLASSIC,
    val intensity: Intensity = Intensity.MEDIUM,
    val currentPrompt: Prompt? = null,
    val history: Set<String> = emptySet(),
    val round: Int = 0,
    val customPrompts: List<Prompt> = emptyList()
) {
    val currentPlayer: Player? get() = players.getOrNull(activePlayer)
}

object GameEngine {
    private val prompts = listOf(
        Prompt("t01","Jaka decyzja z ostatniego roku dała Ci najwięcej satysfakcji?",PromptKind.TRUTH,Intensity.EASY),
        Prompt("t02","Jaki talent chciał(a)byś mieć przez jeden dzień?",PromptKind.TRUTH,Intensity.EASY),
        Prompt("t03","Co najbardziej Cię rozśmiesza, choć trudno to wyjaśnić?",PromptKind.TRUTH,Intensity.EASY),
        Prompt("t04","Jakie pierwsze wrażenie zwykle robisz na innych?",PromptKind.TRUTH,Intensity.MEDIUM),
        Prompt("t05","Jaka była Twoja najbardziej spontaniczna decyzja?",PromptKind.TRUTH,Intensity.MEDIUM),
        Prompt("t06","Czego chciał(a)byś spróbować, ale jeszcze nie było okazji?",PromptKind.TRUTH,Intensity.MEDIUM),
        Prompt("t07","Opowiedz o sytuacji, w której udało Ci się wyjść ze strefy komfortu.",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("t08","Jaką cechę u siebie najbardziej doceniasz?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("d01","Przez następną rundę mów jak prezenter wiadomości.",PromptKind.DARE,Intensity.EASY),
        Prompt("d02","Wymyśl krótkie hasło reklamowe dla osoby po lewej.",PromptKind.DARE,Intensity.EASY),
        Prompt("d03","Pokaż bez słów swój ulubiony sposób spędzania wolnego czasu.",PromptKind.DARE,Intensity.EASY),
        Prompt("d04","Zrób 10-sekundową reklamę przedmiotu, który masz pod ręką.",PromptKind.DARE,Intensity.MEDIUM),
        Prompt("d05","Wymyśl i wykonaj trzy ruchy taneczne.",PromptKind.DARE,Intensity.MEDIUM),
        Prompt("d06","Opowiedz krótką historię, używając trzech słów wskazanych przez grupę.",PromptKind.DARE,Intensity.MEDIUM),
        Prompt("d07","Zaśpiewaj refren dowolnej piosenki przez 15 sekund albo wybierz inne zadanie.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("d08","Przez jedną rundę prowadź komentarz sportowy do zwykłych czynności grupy.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("t09","Jakiej umiejętności nauczyłeś się samodzielnie?",PromptKind.TRUTH,Intensity.EASY),
        Prompt("t10","Jaki drobiazg potrafi poprawić Ci humor?",PromptKind.TRUTH,Intensity.EASY),
        Prompt("t11","Które miejsce chcesz kiedyś odwiedzić?",PromptKind.TRUTH,Intensity.EASY),
        Prompt("t12","Jaka piosenka najlepiej opisuje Twój dzisiejszy nastrój?",PromptKind.TRUTH,Intensity.EASY),
        Prompt("t13","Z czego ostatnio jesteś szczególnie dumny lub dumna?",PromptKind.TRUTH,Intensity.MEDIUM),
        Prompt("t14","Jaką radę z przeszłości pamiętasz do dziś?",PromptKind.TRUTH,Intensity.MEDIUM),
        Prompt("t15","Co najczęściej odkładasz na później?",PromptKind.TRUTH,Intensity.MEDIUM),
        Prompt("t16","Jaki zwyczaj chciałbyś lub chciałabyś wprowadzić do codzienności?",PromptKind.TRUTH,Intensity.MEDIUM),
        Prompt("t17","Jakie przekonanie zmieniło się u Ciebie z biegiem lat?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("t18","W jakiej sytuacji najtrudniej poprosić Ci o pomoc?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("t19","Co chciałbyś lub chciałabyś częściej mówić bliskim osobom?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("t20","Jaki błąd nauczył Cię czegoś ważnego?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("d09","Wymyśl tytuł filmu o dzisiejszym spotkaniu.",PromptKind.DARE,Intensity.EASY),
        Prompt("d10","Przez 20 sekund przedstaw prognozę pogody dla tego pokoju.",PromptKind.DARE,Intensity.EASY),
        Prompt("d11","Wskaż przedmiot w pobliżu i opisz go jak eksponat muzealny.",PromptKind.DARE,Intensity.EASY),
        Prompt("d12","Zrób minę przedstawiającą wybraną emocję, a reszta zgaduje.",PromptKind.DARE,Intensity.EASY),
        Prompt("d13","Wymyśl krótki jingiel dla drużyny.",PromptKind.DARE,Intensity.MEDIUM),
        Prompt("d14","Opowiedz bajkę w trzech zdaniach, w której występuje kubek.",PromptKind.DARE,Intensity.MEDIUM),
        Prompt("d15","Przez następną kolejkę odpowiadaj jak detektyw z filmu noir.",PromptKind.DARE,Intensity.MEDIUM),
        Prompt("d16","Narysuj palcem w powietrzu logo fikcyjnej firmy i je zaprezentuj.",PromptKind.DARE,Intensity.MEDIUM),
        Prompt("d17","Zagraj bez słów scenę z filmu wybranego przez grupę; możesz odmówić wyboru.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("d18","Wymyśl i wygłoś 15-sekundową mowę motywacyjną dla drużyny.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("d19","Przedstaw krótką scenkę, w której zwykły przedmiot ratuje świat.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("d20","Zrób improwizowany wywiad z wybranym przedmiotem.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("r01","Królewski przywilej: wybierz osobę, która zdecyduje, czy następna runda będzie prawdą czy wyzwaniem.",PromptKind.DARE,Intensity.MEDIUM,2,true),
        Prompt("r02","Królewska karta: wskaż gracza, który otrzyma dodatkowy punkt za wykonanie wybranego przez siebie zadania.",PromptKind.DARE,Intensity.BOLD,2,true)
    )

    fun draw(state: GameState, kind: PromptKind, random: Random = Random): GameState {
        fun available(history: Set<String>) = (prompts + state.customPrompts).filter { prompt ->
            prompt.kind == kind &&
                (state.mode == GameMode.CHAOS || prompt.intensity.ordinal <= state.intensity.ordinal) &&
                (state.mode == GameMode.ROYAL || !prompt.royal) &&
                prompt.id !in history
        }

        // History is shared by truth/dare cards. Recycle only IDs belonging to the
        // requested kind so drawing one deck never erases progress in the other.
        var history = state.history
        var eligible = available(history)
        if (eligible.isEmpty()) {
            val idsForKind = (prompts + state.customPrompts)
                .filter { it.kind == kind }
                .mapTo(mutableSetOf()) { it.id }
            history = history - idsForKind
            eligible = available(history)
        }
        if (eligible.isEmpty()) return state.copy(currentPrompt = null, history = history)

        // Royal cards act as a 15% wildcard draw, rather than being duplicated in the pool.
        val royalDraw = state.mode == GameMode.ROYAL &&
            kind == PromptKind.DARE &&
            eligible.any { it.royal } &&
            random.nextFloat() < 0.15f
        val pool = if (royalDraw) eligible.filter { it.royal } else eligible.filterNot { it.royal }.ifEmpty { eligible }
        val chosen = pool.random(random)
        return state.copy(currentPrompt = chosen, history = history + chosen.id, round = state.round + 1)
    }

    fun award(state: GameState, points: Int): GameState {
        if (state.players.isEmpty()) return state
        val updated = state.players.toMutableList()
        val player = updated[state.activePlayer]
        updated[state.activePlayer] = player.copy(score = (player.score + points).coerceAtLeast(0))
        return state.copy(players = updated)
    }

    fun skip(state: GameState): GameState = state.copy(currentPrompt = null)

    fun nextPlayer(state: GameState): GameState {
        if (state.players.isEmpty()) return state
        return state.copy(activePlayer = (state.activePlayer + 1) % state.players.size, currentPrompt = null)
    }

    fun resetHistory(state: GameState): GameState = state.copy(history = emptySet(), currentPrompt = null)
}
