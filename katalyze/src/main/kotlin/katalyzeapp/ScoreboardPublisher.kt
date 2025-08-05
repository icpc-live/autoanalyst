package katalyzeapp

import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.apache.logging.log4j.kotlin.logger
import org.icpclive.cds.api.*
import org.icpclive.cds.scoreboard.getScoreboardCalculator
import web.Publisher
import web.WebDocument
import java.io.OutputStream
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ScoreboardPublisher(private val publisher: Publisher, private val contestStateTracker: ContestStateTracker) {
    private fun publishScoreboard(fullState: ContestStateTracker.FullState) {
        LOGGER.info("Preparing standings")

        val teamJson = teamsAsJson(fullState.info)
        val standingsJson = standingsAsJson(fullState)

        LOGGER.info("Publishing standings")

        publisher.publish("/scoreboard", JsonDocument(standingsJson))
        publisher.publish("/teams", JsonDocument(teamJson))
    }

    suspend fun work() {
        var nextUpdate = ZERO
        while (true) {
            val fullState = contestStateTracker.state
            if (fullState != null) {
                val contestTime = fullState.info.currentContestTime
                if (contestTime >= nextUpdate) {
                    nextUpdate = contestTime + 1.minutes
                    publishScoreboard(fullState)
                }
            }
            delay(1.seconds)  // Can't sleep longer because we might be in the emulation.
        }
    }

    private fun videoUrl(mediaType: MediaType) = when (mediaType) {
        is MediaType.HLSVideo -> mediaType.url
        is MediaType.M2tsVideo -> mediaType.url
        is MediaType.Video -> mediaType.url
        is MediaType.WebRTCGrabberConnection -> mediaType.url
        is MediaType.WebRTCProxyConnection -> mediaType.url
        else -> ""
    }

    private fun teamsAsJson(info: ContestInfo) = buildJsonArray {
        info.teams.values.forEach { team ->
            addJsonObject {
                put("id", team.id.value)
                put("name", team.fullName)
                put("displayname", team.displayName)
                team.organizationId?.let {
                    put("organization", info.organizations[it]!!.fullName)
                }
                val screenUrls = mutableListOf<String>()
                val cameraUrls = mutableListOf<String>()
                team.medias.forEach { (type, media) ->
                    when (type) {
                        TeamMediaType.SCREEN -> screenUrls.add(videoUrl(media))
                        TeamMediaType.CAMERA -> cameraUrls.add(videoUrl(media))
                        else -> {}
                    }
                }
                putJsonArray("webcams") {
                    cameraUrls.forEach { add(it) }
                }
                putJsonArray("desktops") {
                    screenUrls.forEach { add(it) }
                }
            }
        }
    }

    private fun standingsAsJson(fullState: ContestStateTracker.FullState) = buildJsonArray {
        val contestInfo = fullState.info
        val scoreboardCalculator = getScoreboardCalculator(contestInfo, OptimismLevel.NORMAL)
        val ranking = fullState.ranking
        val contestTime = contestInfo.currentContestTime

        val runsPerTeam = fullState.runs.values.groupBy { it.teamId }
        var isFirst = true

        ranking.order.zip(ranking.ranks).forEach { (teamId, currentRank) ->
            val scoreboardRow = fullState.scoreboardRow(teamId)
            val teamRuns = runsPerTeam.getOrDefault(teamId, emptyList())
            addJsonObject {
                if (isFirst) {
                    isFirst = false
                    put("contest_time", contestTime.inWholeMinutes)
                }
                put("rank", currentRank)
                put("team_id", teamId.value)
                put("main_lang", null as String?)  // TODO: implement from submission history
                putJsonObject("score") {
                    put("num_solved", scoreboardRow.totalScore.toInt())
                    put("total_time", scoreboardRow.penalty.inWholeMinutes)
                }
                putJsonArray("problems") {
                    scoreboardRow.problemResults.zip(contestInfo.scoreboardProblems)
                        .forEach { (problemResult, problem) ->
                            check(problemResult is ICPCProblemResult)
                            addJsonObject {
                                put("problem_id", problem.id.value)
                                put("label", problem.displayName)
                                put("solved", problemResult.isSolved)
                                put(
                                    "num_judged",
                                    problemResult.wrongAttempts + if (problemResult.isSolved) 1 else 0
                                )  // TODO: check me
                                put("num_pending", problemResult.pendingAttempts)
                                problemResult.lastSubmitTime?.let {
                                    val penaltyContribution =
                                        (it + contestInfo.penaltyPerWrongAttempt * problemResult.wrongAttempts).takeIf { problemResult.isSolved }
                                    put("time", penaltyContribution?.inWholeMinutes ?: 0)
                                    put("lastUpd", it.inWholeMinutes)
                                }
                                if (!problemResult.isSolved) putJsonObject("potential") {
                                    val potentialRuns = teamRuns + listOf(
                                        RunInfo(
                                            id = "fictional_run".toRunId(),
                                            teamId = teamId,
                                            problemId = problem.id,
                                            languageId = null,
                                            result = Verdict.Accepted.toICPCRunResult(),
                                            time = contestTime,
                                        )
                                    )
                                    val potentialRow = scoreboardCalculator.getScoreboardRow(contestInfo, potentialRuns)
                                    var potentialRank = currentRank
                                    var nextRow: ScoreboardRow? = null
                                    while (potentialRank > 1) {
                                        val prevRow =
                                            fullState.scoreboardRow(ranking.order[potentialRank - 1])
                                        if (prevRow.isAbove(potentialRow)) break
                                        potentialRank -= 1
                                        nextRow = prevRow
                                    }
                                    put("rank", potentialRank)
                                    if (nextRow?.totalScore == potentialRow.totalScore) {
                                        val margin = nextRow.penalty - potentialRow.penalty
                                        put("before", margin.inWholeMinutes)
                                    }
                                }
                            }
                        }
                }
            }
        }
    }

    private fun ScoreboardRow.isAbove(other: ScoreboardRow): Boolean {
        if (totalScore != other.totalScore) {
            return totalScore > other.totalScore
        }
        if (penalty != other.penalty) {
            return penalty < other.penalty
        }
        if (lastAccepted != other.lastAccepted) {
            return lastAccepted < other.lastAccepted
        }
        return false
    }

    class JsonDocument(private val element: JsonElement) : WebDocument {
        override fun getContentType() = "application/json"

        @OptIn(ExperimentalSerializationApi::class)
        override fun writeContents(target: OutputStream?) {
            require(target != null)
            Json.encodeToStream(element, target)
        }

        override fun isGzipCompressed() = false

    }

    companion object {
        private val LOGGER = logger()
        @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
        private val dispatcher = newSingleThreadContext("ScoreboardPublisher")
    }
}