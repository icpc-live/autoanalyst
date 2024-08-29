package rules_kt

import kotlinx.coroutines.flow.Flow
import model.Commentary
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard

interface RuleInterface {
    fun run(contestFlow: Flow<ContestStateWithScoreboard>): Flow<Commentary>
}