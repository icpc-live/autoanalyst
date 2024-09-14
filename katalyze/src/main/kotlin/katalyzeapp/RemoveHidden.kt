package katalyzeapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.ContestState

fun Flow<ContestState>.removeHiddenRuns() = filter {
        state: ContestState->
    val lastEvent = state.lastEvent
    if (lastEvent is RunUpdate) {
        val runInfo = lastEvent.newInfo
        if (runInfo.isHidden)
            return@filter false
        if (state.infoAfterEvent!!.teams[runInfo.teamId]?.isHidden == true)
            return@filter false
        if (state.infoAfterEvent!!.problems[runInfo.problemId]?.isHidden == true)
            return@filter false
    }
    true
}