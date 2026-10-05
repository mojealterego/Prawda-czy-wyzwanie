package pl.mojealterego.prawda

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {
    @Test fun drawAddsPromptToHistory() {
        val initial = GameState(players = listOf(Player("A")))
        val next = GameEngine.draw(initial, PromptKind.TRUTH, Random(1))
        assertNotNull(next.currentPrompt)
        assertEquals(1, next.history.size)
    }

    @Test fun historyPreventsRepeatsUntilReset() {
        var state = GameState(players = listOf(Player("A")))
        repeat(8) { state = GameEngine.draw(state, PromptKind.TRUTH, Random(it)) }
        assertEquals(8, state.history.size)
        state = GameEngine.resetHistory(state)
        assertTrue(state.history.isEmpty())
    }

    @Test fun pointsAreAppliedToActivePlayer() {
        val initial = GameState(players = listOf(Player("A"), Player("B")))
        val awarded = GameEngine.award(initial, 2)
        assertEquals(2, awarded.players[0].score)
        assertEquals(0, awarded.players[1].score)
    }

    @Test fun nextPlayerRotatesAndWraps() {
        val initial = GameState(players = listOf(Player("A"), Player("B")))
        assertEquals(1, GameEngine.nextPlayer(initial).activePlayer)
        assertEquals(0, GameEngine.nextPlayer(GameEngine.nextPlayer(initial)).activePlayer)
    }

    @Test fun exhaustedDeckRecyclesInsteadOfReturningAnEmptyCard() {
        var state = GameState(
            players = listOf(Player("A")),
            intensity = Intensity.BOLD
        )
        repeat(20) { state = GameEngine.draw(state, PromptKind.TRUTH, Random(it)) }
        assertNotNull(state.currentPrompt)
        assertEquals(20, state.history.size)

        // The built-in deck can grow; exhaust every currently eligible truth card
        // instead of assuming the historical fixed size of 20.
        var recycled = state
        var guard = 0
        while (recycled.history.size >= state.history.size && guard < 200) {
            recycled = GameEngine.draw(recycled, PromptKind.TRUTH, Random(21 + guard))
            guard++
        }
        assertNotNull(recycled.currentPrompt)
        assertTrue("deck should recycle after exhaustion", guard < 200)
        assertTrue(recycled.history.isNotEmpty())
    }

    @Test fun royalModeCanDrawRoyalCardsAndClassicCannot() {
        val royal = GameState(players = listOf(Player("A")), mode = GameMode.ROYAL, intensity = Intensity.BOLD)
        val classic = royal.copy(mode = GameMode.CLASSIC)
        var sawRoyal = false
        repeat(500) { seed ->
            val draw = GameEngine.draw(royal.copy(history = emptySet()), PromptKind.DARE, Random(seed))
            if (draw.currentPrompt?.royal == true) sawRoyal = true
        }
        assertTrue(sawRoyal)
        repeat(100) { seed ->
            val draw = GameEngine.draw(classic.copy(history = emptySet()), PromptKind.DARE, Random(seed))
            assertFalse(draw.currentPrompt?.royal == true)
        }
    }

    @Test fun customPromptCanBeDrawnAndRecorded() {
        val custom = Prompt("user-1", "Własne pytanie", PromptKind.TRUTH, Intensity.EASY, 3)
        val usedBuiltInEasyTruths = setOf("t01", "t02", "t03", "t09", "t10", "t11", "t12")
        val state = GameState(
            players = listOf(Player("A")),
            intensity = Intensity.EASY,
            history = usedBuiltInEasyTruths,
            customPrompts = listOf(custom)
        )

        val draw = GameEngine.draw(state, PromptKind.TRUTH, Random(4))

        assertEquals(custom, draw.currentPrompt)
        assertTrue("user-1" in draw.history)
    }

    @Test fun customPromptRespectsKindAndIntensityFilters() {
        val custom = Prompt("user-bold", "Odważne", PromptKind.TRUTH, Intensity.BOLD)
        val state = GameState(players = listOf(Player("A")), customPrompts = listOf(custom), intensity = Intensity.EASY)
        val draw = GameEngine.draw(state, PromptKind.TRUTH, Random(7))
        assertNotEquals("user-bold", draw.currentPrompt?.id)
    }
    @Test fun redRoomOnlyDrawsBoldCards() {
        val state = GameState(players = listOf(Player("A")), mode = GameMode.RED_ROOM, intensity = Intensity.EASY)
        repeat(30) { seed ->
            val draw = GameEngine.draw(state.copy(history = emptySet()), PromptKind.DARE, Random(seed))
            assertEquals(Intensity.BOLD, draw.currentPrompt?.intensity)
        }
    }

    @Test fun awardBuildsStreakAndSkipResetsIt() {
        val state = GameState(players = listOf(Player("A")))
        val awarded = GameEngine.award(GameEngine.award(state, 2), 3)
        assertEquals(2, awarded.streak)
        assertEquals(2, awarded.bestStreak)
        assertEquals(2, awarded.totalCompleted)
        assertEquals(2, awarded.players.first().completed)
        val skipped = GameEngine.skip(awarded)
        assertEquals(0, skipped.streak)
        assertEquals(1, skipped.totalSkipped)
        assertEquals(1, skipped.players.first().skipped)
    }
    @Test fun diceProducesOnlyOneToSix() {
        repeat(200) { seed -> assertTrue(GameEngine.rollDice(Random(seed)) in 1..6) }
    }

    @Test fun targetRouletteNeverTargetsActivePlayerWhenAlternativesExist() {
        val state = GameState(players = listOf(Player("A"), Player("B"), Player("C")), activePlayer = 1)
        repeat(100) { seed -> assertNotEquals(1, GameEngine.pickTarget(state, Random(seed))) }
    }

    @Test fun targetRouletteReturnsActivePlayerForSoloGame() {
        val state = GameState(players = listOf(Player("Solo")), activePlayer = 0)
        assertEquals(0, GameEngine.pickTarget(state, Random(1)))
    }
    @Test fun leaderboardSortsByScoreThenCompleted() {
        val state = GameState(players = listOf(
            Player("A", score = 4, completed = 1),
            Player("B", score = 7, completed = 1),
            Player("C", score = 7, completed = 3)
        ))
        assertEquals(listOf("C", "B", "A"), GameEngine.leaderboard(state).map { it.name })
    }

    @Test fun restartSessionKeepsPlayersAndSettingsButClearsProgress() {
        val state = GameState(
            players = listOf(Player("A", 9, 4, 1), Player("B", 2, 1, 2)),
            mode = GameMode.ROYAL, intensity = Intensity.BOLD, round = 12,
            history = setOf("t01"), streak = 3, bestStreak = 5, totalCompleted = 5, totalSkipped = 3
        )
        val reset = GameEngine.restartSession(state)
        assertEquals(listOf(0, 0), reset.players.map { it.score })
        assertEquals(GameMode.ROYAL, reset.mode)
        assertEquals(Intensity.BOLD, reset.intensity)
        assertEquals(0, reset.round)
        assertTrue(reset.history.isEmpty())
        assertEquals(0, reset.totalCompleted)
        assertEquals(0, reset.totalSkipped)
    }
}
