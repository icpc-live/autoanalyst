package rules_kt

import kotlinx.coroutines.flow.*
import model.Commentary
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard

abstract class RuleInterface {
    open val filters: Collection<(ContestStateWithScoreboard)->Boolean> = emptyList()
    abstract suspend fun process(contestStateWithScoreboard: ContestStateWithScoreboard): Flow<Commentary>
    fun isApplicable(contestStateWithScoreboard: ContestStateWithScoreboard) = filters.all { filter -> filter(contestStateWithScoreboard) }
    fun run(contestFlow: Flow<ContestStateWithScoreboard>) = flow {
        contestFlow.filter(this@RuleInterface::isApplicable).collect { contestStateWithScoreboard ->
            emitAll(process(contestStateWithScoreboard))
        }
    }
}