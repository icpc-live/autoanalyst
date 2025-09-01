package rules_kt

import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import model.Commentary
import model.EventImportance
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.RunInfo
import org.icpclive.cds.api.RunResult
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard

class SmallFixAfterAccepted : RuleInterface() {
    override val filters = listOf(FlowFilters::isAccepted)

    override suspend fun process(contestStateWithScoreboard: ContestStateWithScoreboard): Flow<Commentary> = flow {
        val state = contestStateWithScoreboard.state
        val runUpdate = state.lastEvent as RunUpdate
        val accepted = (runUpdate.newInfo.result as? RunResult.ICPC)?.verdict?.isAccepted == true
        if (!accepted) return@flow

        val currentRun = runUpdate.newInfo
        val previousIncorrect = findLatestIncorrectBefore(state.runsBeforeEvent.values, currentRun) ?: return@flow

        val keyCurrent = SourceDownloadRegistry.keyFrom(currentRun) ?: return@flow
        val keyPrev = SourceDownloadRegistry.keyFrom(previousIncorrect) ?: return@flow

        val currentDeferred = SourceDownloadRegistry[keyCurrent] ?: return@flow
        val prevDeferred = SourceDownloadRegistry[keyPrev] ?: return@flow

        val loaders = awaitAll(currentDeferred, prevDeferred)
        val currentContent = loaders[0]?.load() ?: return@flow
        val prevContent = loaders[1]?.load() ?: return@flow

        val edits = byLineEditDistance(prevContent, currentContent)
        if (edits <= 2) {
            val commentary = Commentary.fromRunUpdateState(state, EventImportance.AnalystMessage) { teamRef, problemRef ->
                "$teamRef fixed their solution for problem $problemRef with a very short change"
            }
            emit(commentary)
        }
    }

    private fun findLatestIncorrectBefore(allRuns: Collection<RunInfo>, current: RunInfo): RunInfo? {
        val candidates = allRuns.filter {
            it.teamId == current.teamId && it.problemId == current.problemId &&
                    ((it.result as? RunResult.ICPC)?.verdict?.isAccepted == false)
        }
        return candidates.maxByOrNull { it.time }
    }

    private fun byLineEditDistance(a: String, b: String): Int {
        val aLines = a.lines()
        val bLines = b.lines()
        val n = aLines.size
        val m = bLines.size
        val dp = Array(n + 1) { IntArray(m + 1) }
        for (i in 0..n) dp[i][0] = i
        for (j in 0..m) dp[0][j] = j
        for (i in 1..n) {
            for (j in 1..m) {
                val cost = if (aLines[i - 1] == bLines[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[n][m]
    }
} 