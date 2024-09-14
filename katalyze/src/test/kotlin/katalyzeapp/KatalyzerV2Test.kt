package katalyzeapp

import config.ApplicationConfig
import config.CdsConfig
import config.DatabaseConfig
import config.KatalyzerConfig
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rules_kt.*
import kotlin.time.Duration.Companion.seconds


class KatalyzerV2Test {

    @Test
    fun run() {
        val config = ApplicationConfig(
            database = DatabaseConfig.TestDBConfig(useFakeDb = true),
            cds = CdsConfig.LocalPath(this.javaClass.getResource("/wf47-event-feed-live.ndjson")!!.path, "wf47_finals"),
            katalyzer = KatalyzerConfig(
                db = KatalyzerConfig.DB(true),
                rules = listOf(
                    AllProblemsSolved(),
                    AllTeamsSolvedOneProblem(),
                    ContestStatusTransitions(),
                    ProblemFirstSolved(),
                    RankingChange(10, 20),
                    RankPredictor(12, 25),
                    RejectedSubmissions(24),
                ),
                web = KatalyzerConfig.Web(true, port = 8099),
            )
        )
        val katalyzer = KatalyzerV2(config)
        assertThrows<TimeoutCancellationException> {
            runBlocking {
                withTimeout(300.seconds) {
                    katalyzer.run()
                }
            }
        }
    }
}