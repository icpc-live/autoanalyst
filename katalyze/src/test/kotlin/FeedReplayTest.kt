import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.icpclive.cds.adapters.addFirstToSolves
import org.icpclive.cds.adapters.processHiddenProblems
import org.icpclive.cds.adapters.processHiddenTeamsAndGroups
import org.icpclive.cds.api.OptimismLevel
import org.icpclive.cds.plugins.clics.ClicsFeed
import org.icpclive.cds.plugins.clics.ClicsSettings
import org.icpclive.cds.plugins.clics.FeedVersion
import org.icpclive.cds.scoreboard.calculateScoreboard
import org.icpclive.cds.settings.EmulationSettings
import org.icpclive.cds.settings.UrlOrLocalPath
import org.icpclive.cds.settings.toFlow
import rules_kt.*
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class FeedReplayTest : DbTestBase(){
    @Test
    fun testFeedReplay() {
        val feedPath = this.javaClass.getResource("/wf47-event-feed-live.ndjson")!!.path
        val settings = ClicsSettings (
            feeds = listOf(
                ClicsFeed(
                    source = UrlOrLocalPath.Local(Path(feedPath)),
                    eventFeedPath = "",
                    eventFeedName = "",
                    contestId = "",
                    feedVersion = FeedVersion.`2023_06`,
                )
            )
        ) {
            emulation = EmulationSettings(speed=1000.0, startTime=Clock.System.now() + 2.seconds)
        }
        val contestFlow = settings.toFlow().addFirstToSolves().processHiddenProblems().processHiddenTeamsAndGroups()
            .calculateScoreboard(OptimismLevel.NORMAL)
        val commentaryFLow = flow {
            val rules = listOf(
                AllProblemsSolved(),
                AllTeamsSolvedOneProblem(),
                ContestInfoDb(db, "test"),
                ContestStatusTransitions(),
                JudgementsDb(db),
                ProblemFirstSolved(),
                RankingChange(5, 10),
                RankPredictor(rankThreshold = 10, freezeRankThreshold = 25),
                RejectedSubmissions(10)
            )
            contestFlow.collect {
                entry ->
                rules.forEach { rule ->
                    if (rule.isApplicable(entry)) {
                        emitAll(rule.process(entry))
                    }
                }
            }
        }
        runBlocking {
            commentaryFLow.collect() {
                println(it)
            }
        }
    }
}