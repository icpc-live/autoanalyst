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

class ProblemFirstSolvedTest {
    @Test
    fun simpleTest() {
        val contestUpdates = emulateContest(2, 2) {
            start()
            var runId = submit(team(1), problem(1), 1.minutes)
            judge(runId, Verdict.Accepted, 2.minutes)
            runId = submit(team(2), problem(1), 3.minutes)
            judge(runId, Verdict.WrongAnswer, 4.minutes)
            runId = submit(team(2), problem(2), 5.minutes)
            judge(runId, Verdict.Accepted, 6.minutes)
            judge(runId, Verdict.WrongAnswer, 7.minutes)
            judge(runId, Verdict.Accepted, 8.minutes)
        }
        val fullFlow = contestUpdates.addFirstToSolves()
            .calculateScoreboard(OptimismLevel.NORMAL)
        runBlocking {
            val commentaries = ProblemFirstSolved().run(fullFlow).toList()
            assertEquals(4, commentaries.size)
            with (commentaries[0]) {
                assertEquals(2.minutes, contestTime)
                assertEquals("{team:1} is the first team to solve problem {problem:A}", message)
            }
            with (commentaries[1]) {
                assertEquals(6.minutes, contestTime)
                assertEquals("{team:2} is the first team to solve problem {problem:B}", message)
            }
            with (commentaries[2]) {
                assertEquals(7.minutes, contestTime)
                assertEquals("{team:2} submission on {problem:B} is no longer the first solve", message)
            }
            with (commentaries[3]) {
                assertEquals(8.minutes, contestTime)
                assertEquals("{team:2} is the first team to solve problem {problem:B}", message)
            }
        }
    }
}