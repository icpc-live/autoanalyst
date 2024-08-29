package rules_kt

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import model.Commentary
import model.EventImportance
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard

class AllProblemsSolved: RuleInterface {
    override fun run(contestFlow: Flow<ContestStateWithScoreboard>): Flow<Commentary> =
        contestFlow.filterFirstSolves().filterByTrigger { contestInfo, runs ->
            runs.filter { it.isAccepted() }.map { it.problemId }.toSet()
                .containsAll(contestInfo.scoreboardProblems.map { it.id })
        }.map { it ->
            Commentary.fromRunUpdateState(
                it.state, EventImportance.Breaking
            ) { _, _ -> "All problems have now been solved" }
        }
}