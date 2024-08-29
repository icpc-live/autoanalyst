package rules_kt

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import model.Commentary
import model.EventImportance
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.RunResult
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard

class ProblemFirstSolved: RuleInterface {
    override fun run(contestFlow: Flow<ContestStateWithScoreboard>): Flow<Commentary> = flow {
        contestFlow.filterFirstSolves().collect collect@{ stateWithScoreboard ->
            val runInfo = (stateWithScoreboard.state.lastEvent as RunUpdate).newInfo
            val runResult = runInfo.result as RunResult.ICPC

            if (runResult.isFirstToSolveRun) {
                emit(Commentary.fromRunUpdateState(
                    stateWithScoreboard.state, EventImportance.Breaking
                ) { teamRef, problemRef ->
                    "$teamRef is the first team to solve problem $problemRef"
                })
            } else {
                emit(Commentary.fromRunUpdateState(
                    stateWithScoreboard.state, EventImportance.Breaking
                ) { teamRef, problemRef ->
                    "$teamRef submission on $problemRef is no longer the first solve"
                } )
            }
        }
    }
}