package rules_kt

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import model.Commentary
import model.EventImportance
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.ContestStatus
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard
import kotlin.time.Duration

class ContestStatusTransitions : RuleInterface {
    override fun run(contestFlow: Flow<ContestStateWithScoreboard>): Flow<Commentary> = flow {
        contestFlow.filterContestInfoUpdates().collect {
            val oldInfo = it.state.infoBeforeEvent
            val newInfo = it.state.infoAfterEvent
            if (!hasStarted(oldInfo) && hasStarted(newInfo)) {
                emit(
                    Commentary.fromContestInfoUpdate(
                        it.state,
                        Duration.ZERO,
                        EventImportance.Breaking,
                        "Contest has started"
                    )
                )
            }
            if (!isFrozen(oldInfo) && isFrozen(newInfo)) {
                emit(
                    Commentary.fromContestInfoUpdate(
                        it.state,
                        newInfo!!.freezeTime!!,
                        EventImportance.Breaking,
                        "Scoreboard is now frozen until the end of the contest"
                    )
                )
            }
            if (!hasFinished(oldInfo) && hasFinished(newInfo)) {
                emit(
                    Commentary.fromContestInfoUpdate(
                        it.state,
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