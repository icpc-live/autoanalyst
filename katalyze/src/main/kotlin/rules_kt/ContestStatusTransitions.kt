package rules_kt

import kotlinx.coroutines.flow.flow
import model.Commentary
import model.EventImportance
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.ContestStatus
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard
import kotlin.time.Duration

class ContestStatusTransitions : RuleInterface() {
    override val filters = listOf(FlowFilters::isContestInfoUpdate)
    override suspend fun process(contestStateWithScoreboard: ContestStateWithScoreboard) = flow {
        with(contestStateWithScoreboard) {
            val oldInfo = state.infoBeforeEvent
            val newInfo = state.infoAfterEvent
            if (!hasStarted(oldInfo) && hasStarted(newInfo)) {
                emit(
                    Commentary.fromContestInfoUpdate(
                        state,
                        Duration.ZERO,
                        EventImportance.Breaking,
                        "Contest has started"
                    )
                )
            }
            if (!isFrozen(oldInfo) && isFrozen(newInfo)) {
                emit(
                    Commentary.fromContestInfoUpdate(
                        state,
                        newInfo!!.freezeTime!!,
                        EventImportance.Breaking,
                        "Scoreboard is now frozen until the end of the contest"
                    )
                )
            }
            if (!hasFinished(oldInfo) && hasFinished(newInfo)) {
                emit(
                    Commentary.fromContestInfoUpdate(
                        state,
                        newInfo!!.contestLength,
                        EventImportance.Breaking,
                        "Contest is now over"
                    )
                )
            }
        }
    }

    private fun hasStarted(info: ContestInfo?): Boolean {
        if (info == null) {
            return false
        }
        return info.status !is ContestStatus.BEFORE
    }

    private fun hasFinished(info: ContestInfo?): Boolean {
        if (info == null) {
            return false
        }
        return info.status is ContestStatus.OVER || info.status is ContestStatus.FINALIZED
    }

    private fun isFrozen(info: ContestInfo?): Boolean {
        if (info == null) {
            return false
        }
        return (info.status as? ContestStatus.RUNNING)?.frozenAt != null
    }
}