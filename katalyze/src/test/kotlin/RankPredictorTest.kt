import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.icpclive.cds.adapters.addFirstToSolves
import org.icpclive.cds.api.OptimismLevel
import org.icpclive.cds.api.Verdict
import org.icpclive.cds.scoreboard.calculateScoreboard
import rules_kt.RankPredictor
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.time.Duration.Companion.minutes

class RankPredictorTest {
    @Test
    fun testRunMethodEmitsCorrectCommentary() {
        val contestUpdates = emulateContest(5, 2) {
            start()
            for (i in 1..3) {
                judge(submit(team(i), problem(1), i.minutes), Verdict.Accepted, i.minutes)
            }
            judge(submit(team(1), problem(2), 4.minutes), Verdict.WrongAnswer, 4.minutes)
            judge(submit(team(2), problem(2), 5.minutes), Verdict.Accepted, 5.minutes)
            submit(team(2), problem(2), 6.minutes)
            submit(team(3), problem(2), 6.minutes)
            submit(team(4), problem(2), 7.minutes)
            submit(team(1), problem(2), 8.minutes)
            submit(team(1), problem(2), 9.minutes)  // Would be skipped
            freeze()
            submit(team(1), problem(2), 241.minutes)
            submit(team(5), problem(1), 241.minutes)
        }
        val fullFlow = contestUpdates.addFirstToSolves()
            .calculateScoreboard(OptimismLevel.NORMAL)
        runBlocking {
            val commentaries = RankPredictor(rankThreshold = 3).run(fullFlow).toList()
            assertContentEquals(listOf(
                "{team:1} submitted a solution for {problem:A}. If correct, they might lead the competition",
                "{team:2} submitted a solution for {problem:A}. If correct, they might become the runner-up",
                "{team:3} submitted a solution for {problem:A}. If correct, they might get rank 3",
                "{team:1} submitted a solution for {problem:B}. If correct, they might further extend their lead",
                "{team:2} submitted a solution for {problem:B}. If correct, they might lead the competition",
                "Despite already having solved it, {team:2} submitted a solution for {problem:B}",
                "{team:3} submitted a solution for {problem:B}. If correct, they might become the runner-up",
                "{team:1} submitted a solution for {problem:B}. If correct, they might still be the runner-up",
                "{team:1} submitted a solution for {problem:B}",
                ), commentaries.map { it.message })
        }
    }
}