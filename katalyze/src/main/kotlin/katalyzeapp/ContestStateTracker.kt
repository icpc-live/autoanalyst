package katalyzeapp

import org.icpclive.cds.api.*
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard
import org.icpclive.cds.scoreboard.Ranking
import java.util.concurrent.atomic.AtomicReference

class ContestStateTracker {
    data class FullState(val info: ContestInfo, val runs: Map<RunId, RunInfo>, val ranking: Ranking, val scoreboardRow: (TeamId) -> ScoreboardRow)

    private val atomicState = AtomicReference<FullState?>(null)

    val state: FullState?
        get() = atomicState.get()

    fun update(flowState: ContestStateWithScoreboard) {
        val info = flowState.state.infoAfterEvent
        if (info == null) {
            atomicState.set(null)
        } else {
            atomicState.set(FullState(info, flowState.state.runsAfterEvent, flowState.rankingAfter, flowState::scoreboardRowAfter))
        }
    }
}
