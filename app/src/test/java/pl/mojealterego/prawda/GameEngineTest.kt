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
}
