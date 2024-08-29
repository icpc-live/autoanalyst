package rules_kt

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import model.Commentary
import model.EventImportance
import org.apache.logging.log4j.kotlin.logger
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard


class RankingChange(private val breakingPrioRanks: Int, private val normalPrioRanks: Int) : RuleInterface {

    private fun problemsAsText(nProblems: Int): String {
        return if (nProblems == 1) {
            "1 problem"
        } else {
            String.format("%d problems", nProblems)
        }
    }

    override fun run(contestFlow: Flow<ContestStateWithScoreboard>): Flow<Commentary> = flow {
        contestFlow.collect collect@{ state ->
            val oldScoreboard = state.rankingBefore
            val newScoreboard = state.rankingAfter
            val runUpdate = (state.state.lastEvent as? RunUpdate) ?: return@collect
            val teamId = runUpdate.newInfo.teamId
            val problemId = runUpdate.newInfo.problemId
            val oldScoreboardRow = state.scoreboardRowBefore(teamId)
            val newScoreboardRow = state.scoreboardRowAfter(teamId)
            val oldProblemResult = oldScoreboardRow.getResultByProblemId(problemId, state.state.infoBeforeEvent!!)!!
            val newProblemResult = newScoreboardRow.getResultByProblemId(problemId, state.state.infoAfterEvent!!)!!
            check(oldProblemResult != newProblemResult) {
                "Scoreboard didn't update after a submission update:\n$state"
            }

            val wasSolved = oldProblemResult.isSolved
            val isSolved = newProblemResult.isSolved
            if (wasSolved == isSolved) return@collect

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
                        state.state, importance
                    ) { teamRef, problemRef ->
                        "$teamRef solves its first problem: $problemRef, and is ${
                            rankString(
                                current = rankAfter,
                                previous = oldScoreboard.ranks.size
                            )
                        }"
                    })
                }

                wasSolved -> {
                    check(!isSolved)
                    emit(Commentary.fromRunUpdateState(
                        state.state, EventImportance.Breaking
                    ) { teamRef, problemRef ->
                        "$teamRef is now ${
                            rankString(
                                current = rankAfter, previous = rankBefore
                            )
                        } after $problemRef was rejudged"
                    })
                }

                rankAfter == 1 && rankBefore == 1 -> {
                    emit(Commentary.fromRunUpdateState(state.state, importance) { teamRef, problemRef ->
                        "$teamRef extends its lead by solving $problemRef. It has now solved $solvedProblemsText"
                    })
                }

                rankAfter <= rankBefore -> {
                    emit(Commentary.fromRunUpdateState(state.state, importance) { teamRef, problemRef ->
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