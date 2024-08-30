package rules_kt

import org.icpclive.cds.InfoUpdate
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.RunInfo
import org.icpclive.cds.api.RunResult
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard


object FlowFilters {
    fun isSubmission(contestStateWithScoreboard: ContestStateWithScoreboard): Boolean {
        val newInfo = (contestStateWithScoreboard.state.lastEvent as? RunUpdate)?.newInfo ?: return false
        val oldInfo = contestStateWithScoreboard.state.runsBeforeEvent[newInfo.id]
        return oldInfo == null
    }

    fun isAccepted(contestStateWithScoreboard: ContestStateWithScoreboard): Boolean {
        val newInfo = (contestStateWithScoreboard.state.lastEvent as? RunUpdate)?.newInfo ?: return false
        val newJudgement = (newInfo.result as? RunResult.ICPC) ?: return false
        return newJudgement.verdict.isAccepted
    }

    fun isICPCJudgement(contestStateWithScoreboard: ContestStateWithScoreboard): Boolean {
        val newInfo = (contestStateWithScoreboard.state.lastEvent as? RunUpdate)?.newInfo ?: return false
        val oldInfo = contestStateWithScoreboard.state.runsBeforeEvent[newInfo.id]
        val newJudgement = (newInfo.result as? RunResult.ICPC) ?: return false
        val oldJudgement = oldInfo?.result as? RunResult.ICPC
        return newJudgement.verdict != oldJudgement?.verdict
    }

    fun isFirstSolve(contestStateWithScoreboard: ContestStateWithScoreboard): Boolean {
        val newInfo = (contestStateWithScoreboard.state.lastEvent as? RunUpdate)?.newInfo ?: return false
        val oldInfo = contestStateWithScoreboard.state.runsBeforeEvent[newInfo.id]
        val newJudgement = (newInfo.result as? RunResult.ICPC) ?: return false
        val oldJudgement = oldInfo?.result as? RunResult.ICPC
        return newJudgement.isFirstToSolveRun != (oldJudgement?.isFirstToSolveRun ?: false)
    }

    fun isContestInfoUpdate(contestStateWithScoreboard: ContestStateWithScoreboard): Boolean {
        return contestStateWithScoreboard.state.lastEvent is InfoUpdate
    }

    fun byTrigger(trigger: (ContestInfo, Iterable<RunInfo>) -> Boolean) =
        fun(contestStateWithScoreboard: ContestStateWithScoreboard): Boolean {
            val infoBefore = contestStateWithScoreboard.state.infoBeforeEvent ?: return false
            val infoAfter = contestStateWithScoreboard.state.infoAfterEvent ?: return false
            return !trigger(infoBefore, contestStateWithScoreboard.state.runsBeforeEvent.values) && trigger(
                infoAfter, contestStateWithScoreboard.state.runsAfterEvent.values
            )
        }
}
