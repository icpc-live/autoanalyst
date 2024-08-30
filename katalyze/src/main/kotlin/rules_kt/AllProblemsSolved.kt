package rules_kt

import kotlinx.coroutines.flow.flow
import model.Commentary
import model.EventImportance
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard

class AllProblemsSolved : RuleInterface() {
    override val filters = listOf(FlowFilters::isFirstSolve, FlowFilters.byTrigger { contestInfo, runs ->
        runs.filter { it.isAccepted() }.map { it.problemId }.toSet()
            .containsAll(contestInfo.scoreboardProblems.map { it.id })
    })

    override suspend fun process(contestStateWithScoreboard: ContestStateWithScoreboard) = flow{
        emit(Commentary.fromRunUpdateState(
            contestStateWithScoreboard.state, EventImportance.Breaking
        ) { _, _ -> "All problems have now been solved" })
    }
}