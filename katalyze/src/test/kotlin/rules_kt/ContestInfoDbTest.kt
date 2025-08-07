package rules_kt

import DbTestBase
import emulateContest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import model.dsl.v1.Contests
import model.dsl.v1.Problems
import model.dsl.v1.Teams
import org.icpclive.cds.adapters.addFirstToSolves
import org.icpclive.cds.api.OptimismLevel
import org.icpclive.cds.api.Verdict
import org.icpclive.cds.scoreboard.calculateScoreboard
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes


class ContestInfoDbTest : DbTestBase() {
    @Test
    fun testSimple() {
        val contestUpdates = emulateContest(2, 2) {
            start()
            var runId = submit(team(1), problem(1), 1.minutes)
            judge(runId, Verdict.WrongAnswer, 2.minutes)
            runId = submit(team(1), problem(1), 3.minutes)
            judge(runId, Verdict.Accepted, 4.minutes)
            runId = submit(team(2), problem(1), 5.minutes)
            judge(runId, Verdict.WrongAnswer, 6.minutes)
            runId = submit(team(2), problem(2), 7.minutes)
            judge(runId, Verdict.Accepted, 8.minutes)
            judge(runId, Verdict.WrongAnswer, 9.minutes)
            freeze()
            finish()
        }
        val fullFlow = contestUpdates.addFirstToSolves()
            .calculateScoreboard(OptimismLevel.NORMAL)
        runBlocking {
            ContestInfoDb(db, "intergalactic-finals-3012").run(fullFlow).collect()
        }
        transaction(db) {
            assertEquals(1, Contests.selectAll().count())
            assertEquals(2, Problems.selectAll().count())
            assertEquals(2, Teams.selectAll().count())
        }
    }
}