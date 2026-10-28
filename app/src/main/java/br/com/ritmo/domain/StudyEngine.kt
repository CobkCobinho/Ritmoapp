package br.com.ritmo.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

enum class Phase(val title: String, val minutes: Int) {
    FOCUS("Hora de focar", 25), SHORT_BREAK("Pausa curta", 5), LONG_BREAK("Pausa longa", 15)
}
data class Subject(val id: String, val name: String)
data class StudySession(
    val id: String, val subjectId: String, val subjectName: String,
    val durationSeconds: Int, val completedAt: Long
)
data class TimerState(
    val phase: Phase = Phase.FOCUS,
    val durationSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val deadline: Long? = null,
    val subjectId: String? = null,
    val sessionId: String? = null
) {
    val running: Boolean get() = deadline != null
    val started: Boolean get() = sessionId != null
    fun remainingAt(now: Long): Int = deadline?.let {
        // Arredondar para cima evita exibir 00:00 antes da conclusão real.
        ((it - now).coerceAtLeast(0L).plus(999L) / 1000L).toInt().coerceAtMost(durationSeconds)
    } ?: remainingSeconds
}
data class StudyState(
    val subjects: List<Subject> = listOf(Subject("initial", "Programação")),
    val selectedSubjectId: String = "initial",
    val focusMinutes: Int = 25,
    val dailyGoal: Int = 4,
    val completedFocus: Int = 0,
    val timer: TimerState = TimerState(),
    val history: List<StudySession> = emptyList()
) {
    val selectedSubject: Subject get() = subjects.first { it.id == selectedSubjectId }
}

/** Regras sem Android. Receber o relógio permite testar sem esperar 25 minutos. */
object StudyEngine {
    fun advance(state: StudyState, now: Long): StudyState {
        val timer = state.timer
        val deadline = timer.deadline ?: return state
        if (now < deadline) return state
        if (timer.phase != Phase.FOCUS) {
            return state.copy(timer = freshTimer(Phase.FOCUS, state.focusMinutes))
        }
        val completed = state.completedFocus + 1
        val subject = state.subjects.first { it.id == timer.subjectId }
        val session = StudySession(
            id = requireNotNull(timer.sessionId), subjectId = subject.id, subjectName = subject.name,
            durationSeconds = timer.durationSeconds, completedAt = deadline
        )
        val next = if (completed % 4 == 0) Phase.LONG_BREAK else Phase.SHORT_BREAK
        return state.copy(
            completedFocus = completed,
            history = (listOf(session) + state.history).distinctBy { it.id }.take(2000),
            timer = freshTimer(next, state.focusMinutes)
        )
    }
    fun toggle(state: StudyState, now: Long): StudyState {
        val ready = advance(state, now)
        // A conclusão prepara a próxima etapa; o usuário decide quando iniciá-la.
        if (ready != state) return ready
        val timer = state.timer
        return if (timer.running) {
            state.copy(timer = timer.copy(remainingSeconds = timer.remainingAt(now), deadline = null))
        } else {
            state.copy(timer = timer.copy(
                deadline = now + timer.remainingSeconds * 1000L,
                subjectId = timer.subjectId ?: state.selectedSubjectId,
                sessionId = timer.sessionId ?: UUID.randomUUID().toString()
            ))
        }
    }
    fun reset(state: StudyState): StudyState =
        state.copy(timer = freshTimer(state.timer.phase, state.focusMinutes))
    fun skipBreak(state: StudyState): StudyState =
        if (state.timer.phase == Phase.FOCUS) state
        else state.copy(timer = freshTimer(Phase.FOCUS, state.focusMinutes))
    fun selectSubject(state: StudyState, id: String): StudyState =
        if (state.timer.started || state.subjects.none { it.id == id }) state
        else state.copy(selectedSubjectId = id)
    fun addSubject(state: StudyState, name: String): StudyState {
        val cleanName = name.trim().take(40)
        if (cleanName.isEmpty() || state.subjects.any { it.name.equals(cleanName, ignoreCase = true) }) return state
        val subject = Subject(UUID.randomUUID().toString(), cleanName)
        return state.copy(
            subjects = state.subjects + subject,
            selectedSubjectId = if (state.timer.started) state.selectedSubjectId else subject.id
        )
    }
    fun setFocusMinutes(state: StudyState, minutes: Int): StudyState {
        if (state.timer.started || minutes !in listOf(15, 25, 50)) return state
        return state.copy(focusMinutes = minutes, timer = freshTimer(state.timer.phase, minutes))
    }
    fun freshTimer(phase: Phase, focusMinutes: Int): TimerState {
        val seconds = (if (phase == Phase.FOCUS) focusMinutes else phase.minutes) * 60
        return TimerState(phase = phase, durationSeconds = seconds, remainingSeconds = seconds)
    }
    fun sessionsOn(state: StudyState, date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): List<StudySession> =
        state.history.filter { Instant.ofEpochMilli(it.completedAt).atZone(zone).toLocalDate() == date }
}
