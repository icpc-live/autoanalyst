package rules_kt

import kotlinx.coroutines.flow.flow
import model.Commentary
import model.EventImportance
import org.apache.logging.log4j.kotlin.logger
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard


class RankingChange(private val breakingPrioRanks: Int, private val normalPrioRanks: Int) : RuleInterface() {

    private fun problemsAsText(nProblems: Int): String {
        return if (nProblems == 1) {
            "1 problem"
        } else {
            String.format("%d problems", nProblems)
        }
    }

    override val filters = listOf(FlowFilters::isICPCJudgement)

    override suspend fun process(contestStateWithScoreboard: ContestStateWithScoreboard) = flow flow@{
        with(contestStateWithScoreboard) {
            val oldScoreboard = rankingBefore
            val newScoreboard = rankingAfter
            val runUpdate = (state.lastEvent as? RunUpdate) ?: return@flow
            val teamId = runUpdate.newInfo.teamId
            val problemId = runUpdate.newInfo.problemId
            val oldScoreboardRow = scoreboardRowBefore(teamId)
            val newScoreboardRow = scoreboardRowAfter(teamId)
            val oldProblemResult = oldScoreboardRow.getResultByProblemId(problemId, state.infoBeforeEvent!!)!!
            val newProblemResult = newScoreboardRow.getResultByProblemId(problemId, state.infoAfterEvent!!)!!
            check(oldProblemResult != newProblemResult) {
                "Scoreboard didn't update after a submission update:\n$state"
            }

            val wasSolved = oldProblemResult.isSolved
            val isSolved = newProblemResult.isSolved
            if (wasSolved == isSolved) return@flow

            val rankBefore = oldScoreboard.getTeamRank(teamId)
            val rankAfter = newScoreboard.getTeamRank(teamId)
            val importance: EventImportance = when {
                rankAfter <= breakingPrioRanks -> EventImportance.Breaking
                rankAfter <= normalPrioRanks -> EventImportance.Normal
                else -> EventImportance.Whatever
            }

            val solvedProblemsText = problemsAsText(newScoreboardRow.totalScore.toInt())

            when {
                oldScoreboardRow.totalScore == 0.0 && newScoreboardRow.totalScore != 0.0 -> {
                    emit(Commentary.fromRunUpdateState(
                        state, importance
                    ) { teamRef, problemRef ->
                        "$teamRef solves its first problem: $problemRef, and is ${
                            rankString(
                                current = rankAfter, previous = oldScoreboard.ranks.size
                            )
                        }"
                    })
                }

                wasSolved -> {
                    check(!isSolved)
                    emit(Commentary.fromRunUpdateState(
                        state, EventImportance.Breaking
                    ) { teamRef, problemRef ->
                        "$teamRef is now ${
                            rankString(
                                current = rankAfter, previous = rankBefore
                            )
                        } after $problemRef was rejudged"
                    })
                }

                rankAfter == 1 && rankBefore == 1 -> {
                    emit(Commentary.fromRunUpdateState(state, importance) { teamRef, problemRef ->
                        "$teamRef extends its lead by solving $problemRef. It has now solved $solvedProblemsText"
                    })
                }

                rankAfter <= rankBefore -> {
                    emit(Commentary.fromRunUpdateState(state, importance) { teamRef, problemRef ->
                        "$teamRef solves $problemRef. It has solved $solvedProblemsText and is ${
                            rankString(
                                current = rankAfter, previous = rankBefore
                            )
                        }"
                    })
                }

                rankAfter > rankBefore -> {
                    logger.error("Weird! why did we end up here?")
                }
            }
        }
    }
}