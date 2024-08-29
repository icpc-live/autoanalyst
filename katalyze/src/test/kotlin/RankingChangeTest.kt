import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.icpclive.cds.adapters.addFirstToSolves
import org.icpclive.cds.api.OptimismLevel
import org.icpclive.cds.api.Verdict
import org.icpclive.cds.scoreboard.calculateScoreboard
import rules_kt.RankingChange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class RankingChangeTest {
    @Test
    fun testRunMethodEmitsCorrectCommentary() {
        val contestUpdates = emulateContest(2, 2) {
            start()
            var runId = submit(team(1), problem(1), 1.minutes)
            judge(runId, Verdict.Accepted, 2.minutes)
            // At this point team 1 has A solved and is leading.

            runId = submit(team(2), problem(1), 3.minutes)
            judge(runId, Verdict.WrongAnswer, 4.minutes)

            runId = submit(team(2), problem(2), 5.minutes)
            judge(runId, Verdict.Accepted, 6.minutes)
            // Team 2 solved B but from the second attempt (so they are now 2nd).

            judge(runId, Verdict.WrongAnswer, 7.minutes)
            // Team 2 solution gets rejudged and now they have 0 problems.

            judge(runId, Verdict.Accepted, 8.minutes)
            // After another rejudge team 2 gets their B back.
            runId = submit(team(1), problem(2), 9.minutes)
            judge(runId, Verdict.RuntimeError, 10.minutes)
            runId = submit(team(1), problem(2), 10.minutes)
            judge(runId, Verdict.RuntimeError, 11.minutes)
            runId = submit(team(1), problem(2), 11.minutes)
            judge(runId, Verdict.Accepted, 12.minutes)
            runId = submit(team(2), problem(1), 12.minutes)
            judge(runId, Verdict.Accepted, 15.minutes)
        }
        val fullFlow = contestUpdates.addFirstToSolves().calculateScoreboard(OptimismLevel.NORMAL)
        runBlocking {
            val commentaries = RankingChange(breakingPrioRanks = 3, normalPrioRanks = 10).run(fullFlow).toList()
            assertEquals(6, commentaries.size)
            with(commentaries[0]) {
                assertEquals(2.minutes, contestTime)
                assertEquals(
                    "{team:1} solves its first problem: {problem:A}, and is now leading the competition",
                    message
                )
            }
            with(commentaries[1]) {
                assertEquals(6.minutes, contestTime)
                assertEquals("{team:2} solves its first problem: {problem:B}, and is still the runner-up", message)
            }
            with(commentaries[2]) {
                assertEquals(7.minutes, contestTime)
                assertEquals("{team:2} is now still the runner-up after {problem:B} was rejudged", message)
            }
            with(commentaries[3]) {
                assertEquals(8.minutes, contestTime)
                assertEquals("{team:2} solves its first problem: {problem:B}, and is still the runner-up", message)
            }
            with(commentaries[4]) {
                assertEquals(12.minutes, contestTime)
                assertEquals("{team:1} extends its lead by solving {problem:B}. It has now solved 2 problems", message)
            }
            with(commentaries[5]) {
                assertEquals(15.minutes, contestTime)
                assertEquals(
                    "{team:2} solves {problem:A}. It has solved 2 problems and is now leading the competition",
                    message
                )
            }
        }
    }
}