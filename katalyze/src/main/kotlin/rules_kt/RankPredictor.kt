package rules_kt

import kotlinx.coroutines.flow.Flow
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

class RankPredictor(private val rankThreshold: Int): RuleInterface {

    override fun run(contestFlow: Flow<ContestStateWithScoreboard>): Flow<Commentary> = flow {
        contestFlow.filterSubmissionEvents().collect collect@{ state ->
            val runInfo = (state.state.lastEvent as RunUpdate).newInfo
            if (runInfo.result is RunResult.ICPC) {
                // If it's already a tested submission, we don't need to predict anything
                return@collect
            }
            val contestInfo = state.state.infoAfterEvent!!
            val teamId = runInfo.teamId
            val problemId = runInfo.problemId
            val resultForProblem =
                state.scoreboardRowAfter(teamId).getResultByProblemId(problemId, contestInfo) ?: return@collect
            if (resultForProblem.isSolved) {
                emit(Commentary.fromRunUpdateState(state.state, EventImportance.Whatever) { teamRef, problemRef ->
                    "Despite already having solved it, $teamRef submitted a solution for $problemRef"
                })
            } else if (runInfo.time > (contestInfo.freezeTime?: Duration.INFINITE)) {
                if (state.rankingBefore.getTeamRank(teamId) <= rankThreshold) {
                    emit(Commentary.fromRunUpdateState(
                        state.state, EventImportance.Whatever
                    ) { teamRef, problemRef -> "$teamRef submitted a solution for $problemRef" })
                }
            } else if (resultForProblem.pendingAttempts > 1) {
                logger.info(
                    "Skipping rank prediction for team $teamId as it has ${resultForProblem.pendingAttempts} outstanding submissions on problem $problemId."
                )
            } else {
                val optimisticScoreboardCalculator = getScoreboardCalculator(contestInfo, OptimismLevel.OPTIMISTIC)
                val teamRuns = state.state.runsAfterEvent.values.filter { it.teamId == teamId }
                val optimisticScoreboardRow = optimisticScoreboardCalculator.getScoreboardRow(contestInfo, teamRuns)
                val optimisticRanking =
                    optimisticScoreboardCalculator.getRanking(contestInfo, state.rankingAfter.order.associateWith {
                        when (it) {
                            teamId -> optimisticScoreboardRow
                            else -> state.scoreboardRowAfter(it)
                        }
                    })
                val currentRank = if (state.scoreboardRowAfter(teamId).totalScore == 0.0) {
                    state.rankingAfter.order.size + 1
                } else {
                    state.rankingBefore.getTeamRank(teamId)
                }
                val optimisticRank = optimisticRanking.getTeamRank(teamId)
                if (optimisticRank <= rankThreshold) {
                    emit(Commentary.fromRunUpdateState(state.state, EventImportance.Normal) { teamRef, problemRef ->
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
        return String.format("Rank Predictor (rank <= %d)", rankThreshold)
    }
}