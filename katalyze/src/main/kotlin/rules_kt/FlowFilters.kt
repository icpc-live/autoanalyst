package rules_kt

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import org.icpclive.cds.InfoUpdate
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.RunInfo
import org.icpclive.cds.api.RunResult
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard

fun Flow<ContestStateWithScoreboard>.filterSubmissionEvents(): Flow<ContestStateWithScoreboard> = filter filter@{
    contestStateWithScoreboard ->
    val newInfo = (contestStateWithScoreboard.state.lastEvent as? RunUpdate ?: return@filter false).newInfo
    val oldInfo = contestStateWithScoreboard.state.runsBeforeEvent[newInfo.id]
    oldInfo == null
}

fun Flow<ContestStateWithScoreboard>.filterICPCJudgementEvents(): Flow<ContestStateWithScoreboard> = filter filter@ {
        contestStateWithScoreboard ->
    val newInfo = (contestStateWithScoreboard.state.lastEvent as? RunUpdate ?: return@filter false).newInfo
    val oldInfo = contestStateWithScoreboard.state.runsBeforeEvent[newInfo.id]
    val newJudgement = (newInfo.result as? RunResult.ICPC)?: return@filter false
    val oldJudgement = oldInfo?.result as? RunResult.ICPC
    newJudgement.verdict != oldJudgement?.verdict
}

fun Flow<ContestStateWithScoreboard>.filterFirstSolves(): Flow<ContestStateWithScoreboard> = filter filter@ {
        contestStateWithScoreboard ->
    val newInfo = (contestStateWithScoreboard.state.lastEvent as? RunUpdate ?: return@filter false).newInfo
    val oldInfo = contestStateWithScoreboard.state.runsBeforeEvent[newInfo.id]
    val newJudgement = (newInfo.result as? RunResult.ICPC)?: return@filter false
    val oldJudgement = oldInfo?.result as? RunResult.ICPC
    newJudgement.isFirstToSolveRun != (oldJudgement?.isFirstToSolveRun ?: false)
}

fun Flow<ContestStateWithScoreboard>.filterAcceptedEvents() : Flow<ContestStateWithScoreboard> = filterICPCJudgementEvents().filter {
    val runUpdate = it.state.lastEvent as RunUpdate
    val judgement = runUpdate.newInfo.result as RunResult.ICPC
    judgement.verdict.isAccepted
}

fun Flow<ContestStateWithScoreboard>.filterByTrigger(trigger: (contestInfo: ContestInfo, runs: Iterable<RunInfo>)->Boolean) = filter filter@{
    val infoBefore = it.state.infoBeforeEvent ?: return@filter false
    val infoAfter = it.state.infoAfterEvent ?: return@filter  false
    !trigger(infoBefore, it.state.runsBeforeEvent.values) && trigger(infoAfter, it.state.runsAfterEvent.values)
}

fun Flow<ContestStateWithScoreboard>.filterContestInfoUpdates() = filter { it.state.lastEvent is InfoUpdate }