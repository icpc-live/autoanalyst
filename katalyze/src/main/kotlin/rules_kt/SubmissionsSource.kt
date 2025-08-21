package rules_kt

import config.CdsConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import katalyzeapp.toCDSSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.emptyFlow
import model.Commentary
import model.dsl.v1.SubmissionSources
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.Database
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.MediaType
import org.icpclive.cds.ktor.createHttpClient
import org.icpclive.cds.ktor.setupAuth
import org.icpclive.cds.plugins.clics.ClicsSettings
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard
import org.icpclive.cds.settings.UrlOrLocalPath
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import kotlin.coroutines.cancellation.CancellationException

class SubmissionsSource(val db: Database, val config: CdsConfig) : RuleInterface() {
    override val filters = listOf(FlowFilters::isSubmission)

    private val client: HttpClient? = run {
        val cdsSettings = config.toCDSSettings() as? ClicsSettings ?: return@run null
        val auth = (cdsSettings.feeds[0].source as? UrlOrLocalPath.Url)?.auth
        cdsSettings.network.createHttpClient() {
            setupAuth(auth)
        }
    }

    override suspend fun process(contestStateWithScoreboard: ContestStateWithScoreboard) = emptyFlow<Commentary>().also {
        if (client == null) return@also
        val runUpdate = contestStateWithScoreboard.state.lastEvent as RunUpdate
        val runId = runUpdate.newInfo.id.value.toInt()
        val urls = runUpdate.newInfo.sourceFiles.mapNotNull {
            when (it) {
                is MediaType.ZipArchive -> it.url
                else -> null
            }
        }
        if (urls.isEmpty()) return@also
        val joinedUrls = urls.joinToString("\n")

        val alreadyPresent = transaction(db) {
            SubmissionSources.selectAll()
                .where(
                    (SubmissionSources.runId eq runId) and
                    (SubmissionSources.sourceUrls eq joinedUrls)
                )
                .limit(1)
                .toList()
                .isNotEmpty()
        }
        val key = SourceDownloadRegistry.SourceKey(runId, joinedUrls)
        val sourceLoader = SourceLoader {
            // load from db
            val content = transaction(db) {
                SubmissionSources.selectAll()
                    .where(
                        (SubmissionSources.runId eq runId) and
                        (SubmissionSources.sourceUrls eq joinedUrls)
                    )
                    .limit(1)
                    .toList()
                    .firstOrNull()?.get(SubmissionSources.content)
            }
            content.orEmpty()
        }
        if (alreadyPresent) {
            SourceDownloadRegistry.ensureDownload(key) {
                sourceLoader
            }
            return@also
        }

        SourceDownloadRegistry.ensureDownload(key) {
            try {
                val sources = urls.mapNotNull { url ->
                    client.get(url).bodyAsBytes()
                }
                val submissions = sources.map { bytes ->
                    val zip = ZipInputStream(ByteArrayInputStream(bytes))
                    val sourcesContents = mutableListOf<ByteArray>()
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        sourcesContents.add("== File: ${entry.name} ==\n".toByteArray(Charsets.UTF_8))
                        sourcesContents.add(zip.readAllBytes())
                    }
                    sourcesContents.joinToString("") { it.toString(Charsets.UTF_8) } + "\n"
                }
                val firstContent = submissions.firstOrNull() ?: return@ensureDownload null

                transaction(db) {
                    SubmissionSources.upsert {
                        it[SubmissionSources.runId] = runId
                        it[SubmissionSources.sourceUrls] = joinedUrls
                        it[SubmissionSources.content] = firstContent
                    }
                }
                sourceLoader
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                null
            }
        }
    }

}