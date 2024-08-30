package rules_kt

import kotlinx.coroutines.flow.emptyFlow
import model.Commentary
import model.dsl.v1.Submissions
import org.apache.logging.log4j.kotlin.logger
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.RunResult
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import kotlin.time.DurationUnit


class JudgementsDb(val db: Database) : RuleInterface() {

    override val filters = listOf(FlowFilters::isICPCJudgement)

    override suspend fun process(contestStateWithScoreboard: ContestStateWithScoreboard) = emptyFlow<Commentary>().also {
        val contestState = contestStateWithScoreboard.state
        val contestInfo = contestState.infoAfterEvent!!
        val runInfo = (contestState.lastEvent as RunUpdate).newInfo
        val verdict = (runInfo.result as RunResult.ICPC).verdict

        try {
            transaction(db) {
                Submissions.upsert {
                    it[problemId] = contestInfo.problems[runInfo.problemId]!!.displayName
                    it[teamId] = runInfo.teamId.value.toInt()
                    it[langId] = runInfo.languageId?.value ?: ""
                    it[result] = verdict.shortName
                    it[contestTime] = runInfo.time.toInt(DurationUnit.MINUTES)
                    it[submissionId] = runInfo.id.value.toInt()
                }
            }

        } catch (e: Exception) {
            logger.error("Error adding judgement to database: ${e.message}")
        }
    }
}