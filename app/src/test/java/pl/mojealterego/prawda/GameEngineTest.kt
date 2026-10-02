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

        val recycled = GameEngine.draw(state, PromptKind.TRUTH, Random(21))
        assertNotNull(recycled.currentPrompt)
        assertEquals(1, recycled.history.size)
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
}
