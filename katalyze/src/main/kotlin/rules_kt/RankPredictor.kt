package rules_kt

import kotlinx.coroutines.flow.flow
import model.Commentary
import model.EventImportance
import org.apache.logging.log4j.kotlin.logger
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.OptimismLevel
import org.icpclive.cds.api.RunResult
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard
import org.icpclive.cds.scoreboard.getScoreboardCalculator
import kotlin.time.Duration

data class RankPredictor(
    private val rankThreshold: Int,
    private val freezeRankThreshold: Int = rankThreshold,
) : RuleInterface() {
    override val filters = listOf(FlowFilters::isSubmission)
    override suspend fun process(contestStateWithScoreboard: ContestStateWithScoreboard) = flow flow@{
        with(contestStateWithScoreboard) {
            val runInfo = (state.lastEvent as RunUpdate).newInfo
            if (runInfo.result is RunResult.ICPC) {
                // If it's already a tested submission, we don't need to predict anything
                return@flow
            }
            val contestInfo = state.infoAfterEvent!!
            val teamId = runInfo.teamId
            val problemId = runInfo.problemId
            val resultForProblem =
                scoreboardRowAfter(teamId).getResultByProblemId(problemId, contestInfo) ?: return@flow
            if (resultForProblem.isSolved) {
                emit(
                    Commentary.fromRunUpdateState(
                        state,
                        EventImportance.Whatever,
                        tags = listOf("submission")
                    ) { teamRef, problemRef ->
                        "Despite already having solved it, $teamRef submitted a solution for $problemRef"
                    })
            } else if (runInfo.time > (contestInfo.freezeTime ?: Duration.INFINITE)) {
                if (rankingBefore.getTeamRank(teamId) <= freezeRankThreshold) {
                    emit(Commentary.fromRunUpdateState(
                        state, EventImportance.Whatever, tags = listOf("submission")
                    ) { teamRef, problemRef -> "$teamRef submitted a solution for $problemRef" })
                }
            } else if (resultForProblem.pendingAttempts > 1) {
                LOGGER.info(
                    "Skipping rank prediction for team $teamId as it has ${resultForProblem.pendingAttempts} outstanding submissions on problem $problemId."
                )
            } else {
                val optimisticScoreboardCalculator = getScoreboardCalculator(contestInfo, OptimismLevel.OPTIMISTIC)
                val teamRuns = state.runsAfterEvent.values.filter { it.teamId == teamId }
                val optimisticScoreboardRow = optimisticScoreboardCalculator.getScoreboardRow(contestInfo, teamRuns)
                val optimisticRanking =
                    optimisticScoreboardCalculator.getRanking(contestInfo, rankingAfter.order.associateWith {
                        when (it) {
                            teamId -> optimisticScoreboardRow
                            else -> scoreboardRowAfter(it)
                        }
                    })
                val currentRank = if (scoreboardRowAfter(teamId).totalScore == 0.0) {
                    rankingAfter.order.size + 1
                } else {
                    rankingBefore.getTeamRank(teamId)
                }
                val optimisticRank = optimisticRanking.getTeamRank(teamId)
                val rankTags = optimisticRanking.rankTags("submission", teamId)
                if (optimisticRank <= rankThreshold) {
                    emit(
                        Commentary.fromRunUpdateState(
                            state,
                            EventImportance.Normal,
                            tags = rankTags
                        ) { teamRef, problemRef ->
                            "$teamRef submitted a solution for $problemRef. If correct, they might ${
                                futureRankString(
                                    optimisticRank, currentRank
                                )
                            }"
                        })
                }
            }
        }
    }

    override fun toString(): String {
        return "RankPredictor (optimisticRank <= $rankThreshold; after freeze: rank <= $freezeRankThreshold)"
    }

    companion object {
        private val LOGGER = logger()
    }
}