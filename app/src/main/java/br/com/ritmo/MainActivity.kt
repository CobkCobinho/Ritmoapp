package br.com.ritmo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.ritmo.ui.RitmoApp
import br.com.ritmo.ui.RitmoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val model: StudyViewModel = viewModel()
            val state by model.state.collectAsStateWithLifecycle()
            val now by model.now.collectAsStateWithLifecycle()
            RitmoTheme {
                RitmoApp(
                    state = state, now = now,
                    onToggle = model::toggle, onReset = model::reset,
                    onSkipBreak = model::skipBreak, onSelectSubject = model::selectSubject,
                    onAddSubject = model::addSubject, onDuration = model::setDuration,
                    onGoal = model::setGoal
                )
            }
        }
    }
}
