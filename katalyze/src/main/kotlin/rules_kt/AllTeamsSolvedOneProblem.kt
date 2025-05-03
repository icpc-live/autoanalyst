package rules_kt

import kotlinx.coroutines.flow.flow
import model.Commentary
import model.EventImportance
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard


class AllTeamsSolvedOneProblem : RuleInterface() {
    override val filters = listOf(
        FlowFilters::isAccepted,
        FlowFilters.byTrigger { contestInfo, runs ->
            runs.filter { it.isAccepted() }.map { it.teamId }.toSet()
                .containsAll(contestInfo.teams.values.filter { !it.isHidden }.map { it.id })
        },
    )

    override suspend fun process(contestStateWithScoreboard: ContestStateWithScoreboard) = flow {
        emit(
            Commentary.fromRunUpdateState(
                contestStateWithScoreboard.state, EventImportance.Breaking
            ) { _, _ -> "All teams have solved at least one problem" }
        )
    }

    override fun toString(): String {
        return "AllTeamsSolvedOneProblem()"
    }
}