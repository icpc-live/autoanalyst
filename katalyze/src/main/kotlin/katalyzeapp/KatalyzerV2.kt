package katalyzeapp

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import config.ApplicationConfig
import config.DatabaseConfig
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import model.Commentary
import model.dsl.v1.*
import org.icpclive.cds.adapters.*
import org.icpclive.cds.api.OptimismLevel
import org.icpclive.cds.scoreboard.calculateScoreboard
import org.icpclive.cds.settings.toFlow
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import rules_kt.ContestInfoDb
import rules_kt.JudgementsDb
import rules_kt.RuleInterface

class KatalyzerV2(private val config: ApplicationConfig) {
    val db: Database? = if (config.katalyzer.db.enable) {
        Database.connect(HikariDataSource(createHikariConfig(config.database)))
    } else null

    private val contestStateTracker = ContestStateTracker()
    private val contestFlow = config.cds.toCDSSettings().toFlow()
        .applyTuningRules(flowOf(config.katalyzer.advancedProperties.rules))
        .addComputedData {}
        .calculateScoreboard(OptimismLevel.NORMAL)
        .onEach {
            contestStateTracker.update(it)
        }

    private val rules = buildList<RuleInterface> {
        addAll(config.katalyzer.rules.filterNotNull())
        if (db != null) {
            add(ContestInfoDb(db, config.cds.contestId))
            add(JudgementsDb(db))
        }
    }

    private val autoCommentaryFlow: Flow<Commentary> = contestFlow.transform {
        rules.forEach { rule ->
            if (rule.isApplicable(it)) {
                emitAll(rule.process(it))
            }
        }
    }.onCompletion {
        rules.forEach { rule -> rule.onStreamEnd() }
    }

    suspend fun run() = coroutineScope {
        if ((config.database as? DatabaseConfig.TestDBConfig)?.createTables == true) {
            createTables()
        }
        val fullSharedCommentaryFlow = fullCommentaryFlow(this).shareIn(this, SharingStarted.Eagerly, Int.MAX_VALUE)
        if (config.katalyzer.web?.enable == true) {
            launch {
                val server = embeddedServer(Netty, port = config.katalyzer.web.port) {
                    install(CORS) {
                        anyHost()
                    }
                    commentaryMessagesModule(fullSharedCommentaryFlow)
                    scoreboardPublisherModule(contestStateTracker)
                    routing {
                        staticResources("/", "")
                    }
                }

                server.start(wait = true)
            }
        }
        launch {
            fullSharedCommentaryFlow.collect {
                println(it)
            }
        }
        if (db != null) {
            launch {
                streamCommentaryToDB(db, contestStateTracker, fullSharedCommentaryFlow)
            }
        }

    }

    private fun createTables() {
        transaction(db!!) {
            SchemaUtils.createMissingTablesAndColumns(Contests, Entries, Problems, Submissions, TeamRegions, Teams)
        }
    }

    private fun fullCommentaryFlow(scope: CoroutineScope): Flow<Commentary> {
        if (db == null) {
            return autoCommentaryFlow
        }
        val humanMessagesChannel = getHumanMessagesFromDatabase(scope, db, contestStateTracker)
        val autoanalystCommentaryChannel = autoCommentaryFlow.produceIn(scope)
        return mergeCommentaryChannelsByContestTime(
            scope,
            autoanalystCommentaryChannel,
            humanMessagesChannel
        ).consumeAsFlow()
    }

    private fun createHikariConfig(dbConfig: DatabaseConfig): HikariConfig {
        when (dbConfig) {
            is DatabaseConfig.TestDBConfig -> {
                require(dbConfig.useFakeDb) { "TestDBConfig must have testDB set to true" }
                return HikariConfig().apply {
                    jdbcUrl = "jdbc:h2:mem:test;MODE=MYSQL"
                    driverClassName = "org.h2.Driver"
                    maximumPoolSize = 5
                }
            }

            is DatabaseConfig.MySQLConfig -> {
                return HikariConfig().apply {
                    jdbcUrl = "jdbc:mysql://${dbConfig.host}/${dbConfig.databaseName}"
                    driverClassName = "com.mysql.cj.jdbc.Driver"
                    username = dbConfig.user
                    password = dbConfig.password?.value
                    maximumPoolSize = 5
                    transactionIsolation = "TRANSACTION_SERIALIZABLE"
                }
            }
        }
    }
}