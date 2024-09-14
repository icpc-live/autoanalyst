package model

import kotlinx.datetime.Instant
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.api.startTime
import org.icpclive.clics.objects.Commentary as CommentaryInterface
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration

data class Commentary(
    override val id: String = nextId().toString(),
    override val time: Instant,
    override val contestTime: Duration,
    override val message: String,
    override val problemIds: List<String>? = null,
    override val sourceId: String? = null,
    override val submissionIds: List<String>? = null,
    override val tags: List<String> = emptyList(),
    override val teamIds: List<String>? = null,
    val isAutomatic: Boolean = true,
    val importance: EventImportance,
) : CommentaryInterface {

    companion object {
        private val counter = AtomicInteger(0)
        private fun nextId() = counter.incrementAndGet()
        val KATALYZER_USER = "katalyzer"

        fun fromRunUpdateState(
            state: ContestState,
            importance: EventImportance,
            tags: List<String> = emptyList(),
            messageCallback: (teamRef: String, problemRef: String) -> String
        ): Commentary {
            val runInfo = (state.lastEvent as RunUpdate).newInfo
            val contestInfo = state.infoAfterEvent!!
            val contestTime = runInfo.testedTime ?: runInfo.time
            val time = contestInfo.startTime!! + contestTime
            val teamRef = "{team:${runInfo.teamId.value}}"
            val problemRef = "{problem:${runInfo.problemId.value}}"
            return Commentary(
                contestTime = contestTime,
                time = time,
                message = messageCallback(teamRef, problemRef),
                problemIds = listOf(runInfo.problemId.value),
                teamIds = listOf(runInfo.teamId.value),
                submissionIds = listOf(runInfo.id.value),
                importance = importance,
                tags = tags,
            )
        }

        fun fromContestInfoUpdate(
            state: ContestState,
            contestTime: Duration,
            importance: EventImportance,
            message: String
        ): Commentary {
            return Commentary(contestTime = contestTime, time=state.infoAfterEvent!!.startTime!! + contestTime,
                message = message, importance = importance)
        }
    }
}
