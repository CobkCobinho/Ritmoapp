package br.com.ritmo.domain

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StudyEngineTest {
    private val start = 1_700_000_000_000L

    @Test fun pauseAndResumePreserveRemainingTime() {
        val running = StudyEngine.toggle(StudyState(), start)
        val paused = StudyEngine.toggle(running, start + 60_000)
        assertFalse(paused.timer.running)
        assertEquals(24 * 60, paused.timer.remainingSeconds)
        val resumed = StudyEngine.toggle(paused, start + 600_000)
        assertEquals(start + 600_000 + 24 * 60_000L, resumed.timer.deadline)
        assertEquals(running.timer.sessionId, resumed.timer.sessionId)
    }
    @Test fun expiredFocusIsRecordedExactlyOnceAfterReopening() {
        val running = StudyEngine.toggle(StudyState(), start)
        val restored = running.copy()
        val finished = StudyEngine.advance(restored, start + 90 * 60_000L)
        assertEquals(1, finished.history.size)
        assertEquals(start + 25 * 60_000L, finished.history.single().completedAt)
        assertEquals(Phase.SHORT_BREAK, finished.timer.phase)
        assertFalse(finished.timer.running)
        assertEquals(finished, StudyEngine.advance(finished, start + 180 * 60_000L))
    }
    @Test fun fourthCompletedFocusGivesLongBreak() {
        val running = StudyEngine.toggle(StudyState(completedFocus = 3), start)
        val completed = StudyEngine.advance(running, start + 25 * 60_000)
        assertEquals(4, completed.completedFocus)
        assertEquals(Phase.LONG_BREAK, completed.timer.phase)
        assertEquals(15 * 60, completed.timer.remainingSeconds)
    }
    @Test fun breaksNeverCountAsStudy() {
        val state = StudyState(timer = StudyEngine.freshTimer(Phase.SHORT_BREAK, 25))
        val running = StudyEngine.toggle(state, start)
        val completed = StudyEngine.advance(running, start + 5 * 60_000)
        assertEquals(Phase.FOCUS, completed.timer.phase)
        assertTrue(completed.history.isEmpty())
        assertEquals(0, completed.completedFocus)
        assertFalse(completed.timer.running)
    }
    @Test fun resetDiscardsPartialFocusWithoutAddingHistory() {
        val running = StudyEngine.toggle(StudyState(), start)
        val reset = StudyEngine.reset(StudyEngine.toggle(running, start + 60_000))
        assertFalse(reset.timer.started)
        assertEquals(1500, reset.timer.remainingSeconds)
        assertTrue(reset.history.isEmpty())
    }
    @Test fun subjectCannotBeChangedDuringPausedOrRunningSession() {
        val subjects = listOf(Subject("a", "Kotlin"), Subject("b", "Python"))
        val initial = StudyState(subjects = subjects, selectedSubjectId = "a")
        val running = StudyEngine.toggle(initial, start)
        val paused = StudyEngine.toggle(running, start + 60_000)
        assertEquals("a", StudyEngine.selectSubject(running, "b").selectedSubjectId)
        assertEquals("a", StudyEngine.selectSubject(paused, "b").selectedSubjectId)
        assertEquals("b", StudyEngine.selectSubject(StudyEngine.reset(paused), "b").selectedSubjectId)
    }
    @Test fun durationCannotChangeDuringSession() {
        val initial = StudyEngine.setFocusMinutes(StudyState(), 50)
        assertEquals(3000, initial.timer.durationSeconds)
        val running = StudyEngine.toggle(initial, start)
        assertEquals(running, StudyEngine.setFocusMinutes(running, 15))
        assertEquals(initial, StudyEngine.setFocusMinutes(initial, -1))
    }
    @Test fun invalidAndDuplicateSubjectNamesAreRejected() {
        val state = StudyState()
        assertEquals(state, StudyEngine.addSubject(state, "  "))
        assertEquals(state, StudyEngine.addSubject(state, " programação "))
        val added = StudyEngine.addSubject(state, "  Matemática  ")
        assertEquals("Matemática", added.selectedSubject.name)
        assertEquals(2, added.subjects.size)
    }
    @Test fun dailyTotalsUseLocalDateInsteadOfUtcDate() {
        val zone = ZoneId.of("America/Sao_Paulo")
        val instant = java.time.Instant.parse("2026-10-29T01:00:00Z").toEpochMilli()
        val state = StudyState(history = listOf(StudySession("s", "initial", "Programação", 1500, instant)))
        assertEquals(1, StudyEngine.sessionsOn(state, LocalDate.of(2026, 10, 28), zone).size)
        assertEquals(0, StudyEngine.sessionsOn(state, LocalDate.of(2026, 10, 29), zone).size)
    }
    @Test fun exactDeadlineCompletesButOneMillisecondEarlierDoesNot() {
        val running = StudyEngine.toggle(StudyState(), start)
        assertEquals(1, running.timer.remainingAt(start + 1_499_999))
        assertEquals(running, StudyEngine.advance(running, start + 1_499_999))
        assertEquals(1, StudyEngine.advance(running, start + 1_500_000).history.size)
    }
    @Test fun skippingBreakDoesNotGiveExtraSession() {
        val running = StudyEngine.toggle(StudyState(), start)
        val finished = StudyEngine.advance(running, start + 1_500_000)
        val next = StudyEngine.skipBreak(finished)
        assertEquals(Phase.FOCUS, next.timer.phase)
        assertEquals(1, next.history.size)
        assertEquals(1, next.completedFocus)
    }
    @Test fun clickAtDeadlineDoesNotAutoStartTheNextPhase() {
        val running = StudyEngine.toggle(StudyState(), start)
        val next = StudyEngine.toggle(running, start + 1_500_000)
        assertEquals(Phase.SHORT_BREAK, next.timer.phase)
        assertFalse(next.timer.running)
        assertEquals(1, next.history.size)
    }
}
