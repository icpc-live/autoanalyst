package rules_kt

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flow
import model.Commentary
import model.EventImportance
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.RunResult
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard

class RejectedSubmissions(private val normalRankThreashold: Int) : RuleInterface() {
    override val filters =
        listOf(FlowFilters::isICPCJudgement, { !(it.state.lastEvent as RunUpdate).newInfo.isAccepted() })

    override suspend fun process(contestStateWithScoreboard: ContestStateWithScoreboard) = flow flow@{
        with(contestStateWithScoreboard) {
            val runUpdate = state.lastEvent as RunUpdate
            val previousSubmissions =
                state.runsBeforeEvent.values.filter { it.teamId == runUpdate.newInfo.teamId && it.time < runUpdate.newInfo.time }
                    .sortedBy { it.time }.toImmutableList()
            if (previousSubmissions.any { it.isAccepted() }) return@flow
            val rank = rankingAfter.getTeamRank(runUpdate.newInfo.teamId)
            val importance = if (rank <= normalRankThreashold) EventImportance.Normal else EventImportance.Whatever

            emit(Commentary.fromRunUpdateState(state, importance) { teamRef, problemRef ->
                buildString {
                    append("$teamRef fails ")
                    append(if (previousSubmissions.isEmpty()) "its first attempt" else "again")
                    append(" on $problemRef due to ")
                    append(formatShortOutcome(runUpdate.newInfo.result))
                    if (!previousSubmissions.isEmpty()) {
                        append(". Previous attempts:")
                    }
                    for (submission in previousSubmissions) {
                        append(' ')
                        append(formatShortOutcome(submission.result))
                    }
                }
            })
        }
    }

    private fun formatShortOutcome(result: RunResult): String = when (result) {
        is RunResult.ICPC -> result.verdict.run { if (!isAccepted && !isAddingPenalty) "($shortName)" else shortName }
        is RunResult.InProgress -> "(pending)"
        else -> throw IllegalArgumentException("Unexpected result type: $result")
    }
}