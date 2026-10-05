package pl.mojealterego.prawda

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Local, device-only persistence for the active session and user-authored cards. */
object GamePersistence {
    private const val PREFS = "prawda_game_v1"
    private const val KEY_STATE = "active_state"
    private const val KEY_DECK = "custom_deck"

    fun save(context: Context, state: GameState, inProgress: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DECK, JSONArray().apply { state.customPrompts.forEach { put(it.toJson()) } }.toString()).apply()
        if (!inProgress) {
            prefs.edit().remove(KEY_STATE).apply()
            return
        }
        val root = JSONObject()
        root.put("players", JSONArray().apply {
            state.players.forEach { put(JSONObject().put("name", it.name).put("score", it.score).put("completed", it.completed).put("skipped", it.skipped)) }
        })
        root.put("activePlayer", state.activePlayer)
        root.put("mode", state.mode.name)
        root.put("intensity", state.intensity.name)
        root.put("round", state.round)
        root.put("pace", state.pace.name)
        root.put("streak", state.streak)
        root.put("bestStreak", state.bestStreak)
        root.put("totalCompleted", state.totalCompleted)
        root.put("totalSkipped", state.totalSkipped)
        root.put("history", JSONArray().apply { state.history.forEach { put(it) } })
        root.put("customPrompts", JSONArray().apply {
            state.customPrompts.forEach { put(it.toJson()) }
        })
        state.currentPrompt?.let { root.put("currentPrompt", it.toJson()) }
        prefs.edit().putString(KEY_STATE, root.toString()).apply()
    }

    fun loadCustomPrompts(context: Context): List<Prompt> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_DECK, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { i -> runCatching { array.getJSONObject(i).toPrompt() }.getOrNull() }
        }.getOrDefault(emptyList())
    }

    fun load(context: Context): GameState? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_STATE, null) ?: return null
        return runCatching {
            val root = JSONObject(raw)
            val playersJson = root.getJSONArray("players")
            val players = (0 until playersJson.length()).map { i ->
                val item = playersJson.getJSONObject(i)
                Player(item.getString("name"), item.optInt("score", 0), item.optInt("completed", 0), item.optInt("skipped", 0))
            }
            if (players.isEmpty()) return null
            val historyJson = root.optJSONArray("history") ?: JSONArray()
            val customJson = root.optJSONArray("customPrompts") ?: JSONArray()
            val promptJson = root.optJSONObject("currentPrompt")
            GameState(
                players = players,
                activePlayer = root.optInt("activePlayer", 0).coerceIn(players.indices),
                mode = enumValueOr(root.optString("mode"), GameMode.CLASSIC),
                intensity = enumValueOr(root.optString("intensity"), Intensity.MEDIUM),
                currentPrompt = promptJson?.toPrompt(),
                history = (0 until historyJson.length()).map { historyJson.getString(it) }.toSet(),
                round = root.optInt("round", 0).coerceAtLeast(0),
                pace = enumValueOr(root.optString("pace"), ChallengePace.STANDARD),
                streak = root.optInt("streak", 0).coerceAtLeast(0),
                bestStreak = root.optInt("bestStreak", 0).coerceAtLeast(0),
                totalCompleted = root.optInt("totalCompleted", 0).coerceAtLeast(0),
                totalSkipped = root.optInt("totalSkipped", 0).coerceAtLeast(0),
                customPrompts = (0 until customJson.length()).mapNotNull { i ->
                    runCatching { customJson.getJSONObject(i).toPrompt() }.getOrNull()
                }
            )
        }.getOrNull()
    }

    private fun Prompt.toJson() = JSONObject()
        .put("id", id).put("text", text).put("kind", kind.name)
        .put("intensity", intensity.name).put("points", points).put("royal", royal)

    private fun JSONObject.toPrompt() = Prompt(
        id = getString("id"),
        text = getString("text"),
        kind = enumValueOr(getString("kind"), PromptKind.TRUTH),
        intensity = enumValueOr(optString("intensity"), Intensity.MEDIUM),
        points = optInt("points", 1).coerceIn(1, 10),
        royal = optBoolean("royal", false)
    )

    private inline fun <reified T : Enum<T>> enumValueOr(value: String, fallback: T): T =
        runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)
}
