package katalyzeapp

import DbTestBase
import com.sksamuel.hoplite.Masked
import com.sksamuel.hoplite.Secret
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


class KatalyzerV2Test : DbTestBase() {

    @Test
    fun run() {
        val cdsConfig = CdsConfig.ClicsServer(
            baseurl = "https://wfcds.icpc-vcss.org:8443/api",
            contestId = "wf49_practice",
            username = "analytics",
            password = Masked("wfpractice-music-magic-fly"),
            privilegedPassword = Masked(""),
            privilegedUsername = "",
        )
        val config = ApplicationConfig(
            database = DatabaseConfig.TestDBConfig(useFakeDb = true, createTables = false),
            //cds = CdsConfig.LocalPath(this.javaClass.getResource("/wf47-event-feed-live.ndjson")!!.path, "wf47_finals"),
            /* Alternatively: */
            cds = cdsConfig,
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
                    SubmissionsSource(db, cdsConfig)
                ),
                web = KatalyzerConfig.Web(true, port = 8099, edit_activity_enabled = false),
            )
        )
        val katalyzer = KatalyzerV2(config)
        assertThrows<TimeoutCancellationException> {
            runBlocking {
                // Increase timeout if you want to do manual testing.
                withTimeout(30.seconds) {
                    katalyzer.run()
                }
            }
        }
    }
}