package br.com.ritmo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import br.com.ritmo.domain.*
import br.com.ritmo.ui.*
import org.junit.Rule
import org.junit.Test

class RitmoAppTest {
    @get:Rule val compose = createComposeRule()

    @Test fun homeAllowsAddingSubjectAndOpeningProgress() {
        compose.setContent {
            RitmoTheme {
                RitmoApp(StudyState(), 0L, {}, {}, {}, {}, {}, {}, {})
            }
        }
        compose.onNodeWithText("Começar foco").assertIsDisplayed()
        compose.onNodeWithTag("focus-list").performScrollToNode(hasContentDescription("Adicionar matéria"))
        compose.onNodeWithContentDescription("Adicionar matéria").performClick()
        compose.onNodeWithText("Nome da matéria").performTextInput("Kotlin")
        compose.onNodeWithText("Adicionar", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Progresso").performClick()
        compose.onNodeWithText("Sua meta de hoje", substring = true, ignoreCase = true).assertIsDisplayed()
    }
    @Test fun activeTimerDisplaysPauseAndRemainingTime() {
        val running = StudyEngine.toggle(StudyState(), 0L)
        compose.setContent {
            RitmoTheme { RitmoApp(running, 60_000L, {}, {}, {}, {}, {}, {}, {}) }
        }
        compose.onNodeWithText("24:00").assertIsDisplayed()
        compose.onNodeWithText("Pausar").assertIsDisplayed()
    }
}
