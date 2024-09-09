package katalyzeapp

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import config.ApplicationConfig
import config.CdsConfig
import config.DatabaseConfig
import config.KatalyzerConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import model.dsl.v1.*
import org.apache.logging.log4j.kotlin.logger
import org.icpclive.cds.InfoUpdate
import org.icpclive.cds.adapters.*
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.OptimismLevel
import org.icpclive.cds.api.toProblemId
import org.icpclive.cds.api.toTeamId
import org.icpclive.cds.plugins.clics.ClicsFeed
import org.icpclive.cds.plugins.clics.ClicsSettings
import org.icpclive.cds.plugins.clics.FeedVersion
import org.icpclive.cds.scoreboard.calculateScoreboard
import org.icpclive.cds.settings.*
import org.icpclive.clics.v202306.objects.Commentary
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import rules_kt.ContestInfoDb
import rules_kt.JudgementsDb
import rules_kt.RuleInterface
import web.WebPublisher
import javax.sql.DataSource
import kotlin.io.path.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class KatalyzeV2{

    lateinit var config: ApplicationConfig

    private val contestStateTracker = ContestStateTracker()


    fun run() {
        val path = "/Users/yakovdlougach/autoanalyst/katalyze/config.yaml"

        config = ConfigLoaderBuilder.default().addDecoders(
            listOf(
                KatalyzerConfig.RuleInterfaceDecoder(),
                KatalyzerConfig.NoArgumentDecoder(),
                KatalyzerConfig.AdvancedPropertiesDecoder(),
            )
        ).addFileSource(path).build().loadConfigOrThrow<ApplicationConfig>()

        val db = if (config.katalyzer.db.enable) Database.connect(config.database.toDataSource()) else null
        if (db != null) {
            transaction(db) {
                SchemaUtils.createMissingTablesAndColumns(Contests, Entries, Problems, Submissions, TeamRegions, Teams)
                Entries.insert {
                    it[user] = "katalyzer"
                    it[contestTime] = 1
                    it[priority] = 1
                    it[text] = "Hello world"
                }
                // create tables
            }
        }
        val cdsSettings = config.cds.toCDSSettings()
        val contestFlow = cdsSettings.toFlow()
            .applyAdvancedProperties(flowOf(config.katalyzer.advancedProperties))
            .addFirstToSolves()
            .processHiddenProblems()
            .processHiddenTeamsAndGroups()
            .contestState().removeFrozenSubmissions()
            .calculateScoreboard(OptimismLevel.NORMAL)
            .onEach {
                contestStateTracker.update(it)
            }
        var rules = config.katalyzer.getRules()
        if (config.katalyzer.db.enable) {
            check(db != null)
            rules = rules + listOf(
                ContestInfoDb(db, config.cds.contestId),
                JudgementsDb(db)
            )
        }
        val webPublisher =
            if (config.katalyzer.web?.enable == true) WebPublisher(config.katalyzer.web!!.compress) else null
        val autoanalystCommentaryFlow = contestFlow.transform { entry ->
            rules.forEach { rule ->
                if (rule.isApplicable(entry)) {
                    emitAll(rule.process(entry))
                }
            }
        }.onCompletion {
            rules.forEach { it.onStreamEnd() }
        }.let { commentaryFlow ->
            if (config.katalyzer.db.enable) {
                var startFromTime: Duration? = null
                commentaryFlow.onStart {
                    transaction(db) {
                        // select maximum contest time where user is katalyzer
                        val maxContestTime = Entries.contestTime.max()
                        val row = Entries.select(maxContestTime).where(Entries.user eq "katalyzer").firstOrNull()
                        startFromTime = row?.get(maxContestTime)?.minutes
                        if (startFromTime != null) {
                            println("Starting from $startFromTime")
                        }
                    }
                }.onEach { entry ->
                    if (startFromTime != null && entry.contestTime < startFromTime!!) return@onEach
                    transaction(db) {
                        Entries.upsert { row ->
                            row[user] = "katalyzer"
                            row[contestTime] = entry.contestTime.inWholeMinutes.toInt()
                            row[priority] = entry.importance.ordinal
                            row[text] = contestStateTracker.state!!.info.formatDBCommentary(entry.message)
                            row[submissionId] = entry.submissionIds?.firstOrNull()?.toInt()
                        }
                    }
                }
            } else commentaryFlow
        }
        runBlocking {
            val fullCommentaryFlow = if (db != null) {
                val humanMessagesChannel = getHumanMessagesFromDatabase(this, db, contestStateTracker)
                val autoanalystCommentaryChannel = autoanalystCommentaryFlow.produceIn(this)
                mergeCommentaryChannelsByContestTime(
                    this,
                    autoanalystCommentaryChannel,
                    humanMessagesChannel
                ).consumeAsFlow()
            } else autoanalystCommentaryFlow
            val sharedFlow = fullCommentaryFlow.shareIn(this, SharingStarted.Eagerly, Int.MAX_VALUE)
            if (config.katalyzer.web?.enable == true) {
                val jsonFlow = sharedFlow.map {
                    Json.encodeToString(
                        Commentary.serializer(), Commentary(
                            id = it.id,
                            contestTime = it.contestTime,
                            message = it.message,
                            time = it.time,
                            teamIds = it.teamIds,
                            submissionIds = it.submissionIds,
                            tags = it.tags + listOf("importance-${it.importance.name.lowercase()}"),
                        )
                    )

                }.shareIn(this, SharingStarted.Eagerly, Int.MAX_VALUE)
                val ktorServer = embeddedServer(Netty, port = config.katalyzer.web!!.port) {
                    install(CORS) {
                        anyHost()
                    }
                    routing {
                        get("/commentary-messages") {
                            call.respondTextWriter(
                                contentType = ContentType(
                                    "application",
                                    "x-ndjson",
                                    listOf(HeaderValueParam("charset", "utf-8"))
                                )
                            ) {
                                jsonFlow.collect {
                                    withContext(Dispatchers.IO) {
                                        write(it)
                                        write("\n")
                                        flush()
                                    }
                                }
                            }
                        }
                        staticResources("/", "")
                        get("/scoreboard") {
                            call.respondOutputStream(ContentType.Application.Json) {
                                val doc = webPublisher!!.get("/scoreboard")
                                doc?.writeContents(this)
                            }
                        }
                        get("/teams") {
                            call.respondOutputStream(ContentType.Application.Json) {
                                val doc = webPublisher!!.get("/teams")
                                doc?.writeContents(this)
                            }
                        }
                    }
                }
                ktorServer.start(wait = false)
                ScoreboardPublisher(webPublisher!!, contestStateTracker).start()
            }
            sharedFlow.onEach {
                println(it)
            }.launchIn(this)
        }
    }

    companion object {
        private val LOGGER = logger()

        fun CdsConfig.toCDSSettings(): CDSSettings = when (this) {
            is CdsConfig.ClicsServer -> ClicsSettings(
                feeds = listOf(
                    ClicsFeed(
                        source = UrlOrLocalPath.Url(
                            baseurl,
                            auth = Authorization(
                                basic = Authorization.BasicAuth(
                                    login = Credential("login", username),
                                    password = Credential("password", password.value)
                                )
                            )
                        ),
                        contestId = contestId,
                    )
                )
            )

            is CdsConfig.LocalPath -> ClicsSettings(
                feeds = listOf(
                    ClicsFeed(
                        source = UrlOrLocalPath.Local(Path(path)),
                        eventFeedPath = "",
                        eventFeedName = "",
                        contestId = "",
                        feedVersion = FeedVersion.`2023_06`
                    )
                )
            ) {
                emulation = EmulationSettings(speed = 10.0 * 60.0, startTime = Clock.System.now() + 2.seconds)
            }
        }

        fun KatalyzerConfig.getRules(): List<RuleInterface> = rules.filterNotNull()

        fun DatabaseConfig.toDataSource(): DataSource {
            val config = HikariConfig().apply {
                jdbcUrl = "jdbc:h2:mem:test;MODE=MYSQL"
                driverClassName = "org.h2.Driver"
                maximumPoolSize = 5
            }
            /*val config = HikariConfig().apply {
                jdbcUrl = "jdbc:mysql://${host}/${databaseName}"
                driverClassName = "com.mysql.cj.jdbc.Driver"
                username = username
                password = password
                maximumPoolSize = 5
                transactionIsolation = "TRANSACTION_SERIALIZABLE"
            }*/
            return HikariDataSource(config)
        }

        private val problemPattern = "\\{problem:(\\w+)}".toRegex()
        private val teamsPattern = "\\{team:(\\w+)}".toRegex()

        fun ContestInfo.formatDBCommentary(commentary: String): String {
            return commentary.replace(problemPattern) {
                val problem = problems[it.groupValues[1].toProblemId()]
                if (problem == null) {
                    LOGGER.error("Problem ${it.groupValues[1]} not found")
                    it.value
                } else {
                    "#p${problem.displayName}"
                }
            }.replace(teamsPattern) { matchResult ->
                val team = teams[matchResult.groupValues[1].toTeamId()]
                if (team == null) {
                    LOGGER.error("Team ${matchResult.groupValues[1]} not found")
                    matchResult.value
                } else {
                    "#t${team.id}"
                }
            }
        }
    }

}