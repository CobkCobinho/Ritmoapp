package br.com.ritmo.data

import android.content.Context
import androidx.core.content.edit
import br.com.ritmo.domain.*
import org.json.JSONArray
import org.json.JSONObject

/** Um único documento local; sem conta, servidor ou permissão de internet. */
class StudyRepository(context: Context) {
    private val preferences = context.getSharedPreferences("ritmo_v1", Context.MODE_PRIVATE)

    fun load(): StudyState = runCatching {
        val raw = preferences.getString("state", null) ?: return StudyState()
        val json = JSONObject(raw)
        val subjectsJson = json.getJSONArray("subjects")
        val subjects = List(subjectsJson.length()) { index ->
            subjectsJson.getJSONObject(index).let { Subject(it.getString("id"), it.getString("name")) }
        }
        val timerJson = json.getJSONObject("timer")
        val timer = TimerState(
            phase = Phase.valueOf(timerJson.getString("phase")),
            durationSeconds = timerJson.getInt("duration"),
            remainingSeconds = timerJson.getInt("remaining"),
            deadline = if (timerJson.isNull("deadline")) null else timerJson.getLong("deadline"),
            subjectId = if (timerJson.isNull("subject")) null else timerJson.getString("subject"),
            sessionId = if (timerJson.isNull("session")) null else timerJson.getString("session")
        )
        val historyJson = json.getJSONArray("history")
        val history = List(historyJson.length()) { index ->
            historyJson.getJSONObject(index).let {
                StudySession(it.getString("id"), it.getString("subjectId"), it.getString("subjectName"),
                    it.getInt("duration"), it.getLong("completedAt"))
            }
        }
        val state = StudyState(
            subjects, json.getString("selectedSubject"), json.getInt("focusMinutes"),
            json.getInt("dailyGoal"), json.getInt("completedFocus"), timer, history
        )
        require(subjects.isNotEmpty() && subjects.map { it.id }.distinct().size == subjects.size)
        require(subjects.any { it.id == state.selectedSubjectId })
        require(state.focusMinutes in listOf(15, 25, 50) && state.dailyGoal in 1..12)
        require(timer.durationSeconds > 0 && timer.remainingSeconds in 1..timer.durationSeconds)
        require(!timer.started || subjects.any { it.id == timer.subjectId })
        require(!timer.running || timer.started)
        state
    }.getOrDefault(StudyState())

    fun save(state: StudyState) {
        val json = JSONObject().apply {
            put("schema", 1)
            put("subjects", JSONArray().apply {
                state.subjects.forEach { put(JSONObject().put("id", it.id).put("name", it.name)) }
            })
            put("selectedSubject", state.selectedSubjectId)
            put("focusMinutes", state.focusMinutes)
            put("dailyGoal", state.dailyGoal)
            put("completedFocus", state.completedFocus)
            put("timer", JSONObject().apply {
                put("phase", state.timer.phase.name)
                put("duration", state.timer.durationSeconds)
                put("remaining", state.timer.remainingSeconds)
                put("deadline", state.timer.deadline ?: JSONObject.NULL)
                put("subject", state.timer.subjectId ?: JSONObject.NULL)
                put("session", state.timer.sessionId ?: JSONObject.NULL)
            })
            put("history", JSONArray().apply {
                state.history.forEach {
                    put(JSONObject().put("id", it.id).put("subjectId", it.subjectId)
                        .put("subjectName", it.subjectName).put("duration", it.durationSeconds)
                        .put("completedAt", it.completedAt))
                }
            })
        }
        // apply atualiza a memória imediatamente e grava em disco fora da thread da interface.
        preferences.edit { putString("state", json.toString()) }
    }
}
