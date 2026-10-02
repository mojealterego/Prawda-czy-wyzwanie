package pl.mojealterego.prawda

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GamePersistenceTest {
    private lateinit var context: Context

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("prawda_game_v1", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun activeSessionRoundTripsIncludingScoresAndCurrentCard() {
        val card = Prompt("custom-x", "Treść", PromptKind.DARE, Intensity.BOLD, 4)
        val state = GameState(players = listOf(Player("A", 5), Player("B", 2)), activePlayer = 1, mode = GameMode.ROYAL, intensity = Intensity.BOLD, currentPrompt = card, history = setOf("d01", "custom-x"), round = 8, customPrompts = listOf(card))
        GamePersistence.save(context, state, true)
        val restored = GamePersistence.load(context)
        assertNotNull(restored)
        assertEquals(state, restored)
    }

    @Test fun customDeckPersistsAfterSessionEnds() {
        val card = Prompt("custom-y", "Moja karta", PromptKind.TRUTH, Intensity.EASY, 2)
        GamePersistence.save(context, GameState(players = listOf(Player("A")), customPrompts = listOf(card)), false)
        assertNull(GamePersistence.load(context))
        assertEquals(listOf(card), GamePersistence.loadCustomPrompts(context))
    }
}