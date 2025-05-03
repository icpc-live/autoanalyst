package rules_kt

import emulateContest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.icpclive.cds.adapters.addFirstToSolves
import org.icpclive.cds.api.OptimismLevel
import org.icpclive.cds.api.Verdict
import org.icpclive.cds.scoreboard.calculateScoreboard
import kotlin.test.Test

import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class ContestStatusTransitionsTest {
    @Test
    fun simpleTest() {
        val contestUpdates = emulateContest(1, 1) {
            start()
            val runId = submit(team(1), problem(1), 1.minutes)
            judge(runId, Verdict.Accepted, 2.minutes)
            freeze()
            finish()
        }
        val fullFlow = contestUpdates.addFirstToSolves()
            .calculateScoreboard(OptimismLevel.NORMAL)
        runBlocking {
            val commentaries = ContestStatusTransitions().run(fullFlow).toList()
            assertEquals(3, commentaries.size)
            with(commentaries[0]) {
                assertEquals(0.minutes, contestTime)
                assertEquals("Contest has started", message)
            }
            with(commentaries[1]) {
                assertEquals(240.minutes, contestTime)
                assertEquals("Scoreboard is now frozen until the end of the contest", message)
            }
            with(commentaries[2]) {
                assertEquals(300.minutes, contestTime)
                assertEquals("Contest is now over", message)
            }
        }
    }
}