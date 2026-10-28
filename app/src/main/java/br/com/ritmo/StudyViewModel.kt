package br.com.ritmo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.ritmo.data.StudyRepository
import br.com.ritmo.domain.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StudyRepository(application)
    private val _state = MutableStateFlow(StudyEngine.advance(repository.load(), System.currentTimeMillis()))
    val state = _state.asStateFlow()
    private val _now = MutableStateFlow(System.currentTimeMillis())
    val now = _now.asStateFlow()

    init {
        repository.save(_state.value)
        viewModelScope.launch {
            while (true) {
                _now.value = System.currentTimeMillis()
                update { StudyEngine.advance(it, _now.value) }
                delay(250)
            }
        }
    }
    private fun update(transform: (StudyState) -> StudyState) {
        val updated = transform(_state.value)
        if (updated != _state.value) {
            _state.value = updated
            repository.save(updated)
        }
    }
    fun toggle() = update { StudyEngine.toggle(it, System.currentTimeMillis()) }
    fun reset() = update { StudyEngine.reset(StudyEngine.advance(it, System.currentTimeMillis())) }
    fun skipBreak() = update { StudyEngine.skipBreak(StudyEngine.advance(it, System.currentTimeMillis())) }
    fun selectSubject(id: String) = update { StudyEngine.selectSubject(it, id) }
    fun addSubject(name: String) = update { if (it.subjects.size < 24) StudyEngine.addSubject(it, name) else it }
    fun setDuration(minutes: Int) = update { StudyEngine.setFocusMinutes(it, minutes) }
    fun setGoal(goal: Int) = update { it.copy(dailyGoal = goal.coerceIn(1, 12)) }
}
