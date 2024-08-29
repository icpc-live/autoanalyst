package rules_kt

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import model.Commentary
import model.EventImportance
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.RunInfo
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard

class AllTeamsSolvedOneProblem : RuleInterface {
    override fun run(contestFlow: Flow<ContestStateWithScoreboard>): Flow<Commentary> =
        contestFlow.filterAcceptedEvents().filterByTrigger { contestInfo: ContestInfo, runs: Iterable<RunInfo> ->
            runs.filter { it.isAccepted() }.map { it.teamId }.toSet()
                .containsAll(contestInfo.teams.values.filter { !it.isHidden }.map { it.id })
        }.map {
            Commentary.fromRunUpdateState(
                it.state, EventImportance.Breaking
            ) { _, _ -> "All teams have solved at least one problem" }
        }
}