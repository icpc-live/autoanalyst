package rules_kt

import kotlinx.coroutines.flow.*
import model.Commentary
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard

abstract class RuleInterface {
    open val filters: Collection<(ContestStateWithScoreboard) -> Boolean> = emptyList()
    abstract suspend fun process(contestStateWithScoreboard: ContestStateWithScoreboard): Flow<Commentary>
    open suspend fun onStreamEnd() = Unit
    fun isApplicable(contestStateWithScoreboard: ContestStateWithScoreboard) =
        filters.all { filter -> filter(contestStateWithScoreboard) }

    fun run(contestFlow: Flow<ContestStateWithScoreboard>) =
        contestFlow.filter(this::isApplicable).transform { contestStateWithScoreboard ->
            emitAll(process(contestStateWithScoreboard))
        }.onCompletion { onStreamEnd() }
}