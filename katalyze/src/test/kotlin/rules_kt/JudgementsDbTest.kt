package rules_kt

import DbTestBase
import emulateContest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import model.dsl.v1.Submissions
import org.icpclive.cds.adapters.addFirstToSolves
import org.icpclive.cds.api.OptimismLevel
import org.icpclive.cds.api.Verdict
import org.icpclive.cds.scoreboard.calculateScoreboard
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class JudgementsDbTest: DbTestBase() {

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
        }
        val fullFlow = contestUpdates.addFirstToSolves()
            .calculateScoreboard(OptimismLevel.NORMAL)
        runBlocking {
            JudgementsDb(db).run(fullFlow).collect()
        }
        transaction(db) { assertEquals(4, Submissions.selectAll().count()) }
    }
}
