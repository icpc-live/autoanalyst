import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.icpclive.cds.adapters.addFirstToSolves
import org.icpclive.cds.api.OptimismLevel
import org.icpclive.cds.api.Verdict
import org.icpclive.cds.scoreboard.calculateScoreboard
import rules_kt.RejectedSubmissions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class RejectedSubmissionsTest {
    @Test
    fun testRunMethodEmitsCorrectCommentary() {
        val contestUpdates = emulateContest(2, 2) {
            start()
            var runId = submit(team(1), problem(1), 1.minutes)
            judge(runId, Verdict.WrongAnswer, 2.minutes)
            runId = submit(team(1), problem(1), 3.minutes)
            judge(runId, Verdict.CompilationError, 4.minutes)
            runId = submit(team(2), problem(1), 5.minutes)
            judge(runId, Verdict.Accepted, 6.minutes)
            submit(team(1), problem(1), 7.minutes)
            runId = submit(team(1), problem(1), 8.minutes)
            judge(runId, Verdict.RuntimeError, 8.minutes)
        }
        val fullFlow = contestUpdates.addFirstToSolves()
            .calculateScoreboard(OptimismLevel.NORMAL)
        runBlocking {
            val commentaries = RejectedSubmissions(normalRankThreashold = 3).run(fullFlow).toList()
            assertEquals(3, commentaries.size)
            with (commentaries[0]) {
                assertEquals(2.minutes, contestTime)
                assertEquals("{team:1} fails its first attempt on {problem:A} due to WA", message)
            }
            with (commentaries[1]) {
                assertEquals(4.minutes, contestTime)
                assertEquals("{team:1} fails again on {problem:A} due to (CE). Previous attempts: WA", message)
            }
            with (commentaries[2]) {
                assertEquals(8.minutes, contestTime)
                assertEquals("{team:1} fails again on {problem:A} due to RE. Previous attempts: WA (CE) (pending)", message)
            }
        }
    }
}