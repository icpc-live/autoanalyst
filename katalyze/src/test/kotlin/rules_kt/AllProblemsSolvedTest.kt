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

class AllProblemsSolvedTest {
    @Test
    fun simpleTest() {
        val contestUpdates = emulateContest(1, 1) {
            start()
            val runId = submit(team(1), problem(1), 1.minutes)
            judge(runId, Verdict.Accepted, 2.minutes)
        }
        val fullFlow = contestUpdates.addFirstToSolves()
            .calculateScoreboard(OptimismLevel.NORMAL)
        runBlocking {
            val commentaries = AllProblemsSolved().run(fullFlow).toList()
            assertEquals(1, commentaries.size)
            with(commentaries[0]) {
                assertEquals(2.minutes, contestTime)
                assertEquals("All problems have now been solved", message)
            }
        }
    }
}