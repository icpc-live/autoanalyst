package rules_kt

import kotlinx.coroutines.flow.flow
import model.Commentary
import model.EventImportance
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.RunResult
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard

class ProblemFirstSolved : RuleInterface() {
    override val filters = listOf(FlowFilters::isFirstSolve)
    override suspend fun process(contestStateWithScoreboard: ContestStateWithScoreboard) = flow{
        val runInfo = (contestStateWithScoreboard.state.lastEvent as RunUpdate).newInfo
        val runResult = runInfo.result as RunResult.ICPC

        if (runResult.isFirstToSolveRun) {
            emit(Commentary.fromRunUpdateState(
                contestStateWithScoreboard.state, EventImportance.Breaking
            ) { teamRef, problemRef ->
                "$teamRef is the first team to solve problem $problemRef"
            })
        } else {
            emit(Commentary.fromRunUpdateState(
                contestStateWithScoreboard.state, EventImportance.Breaking
            ) { teamRef, problemRef ->
                "$teamRef submission on $problemRef is no longer the first solve"
            })
        }
    }
}