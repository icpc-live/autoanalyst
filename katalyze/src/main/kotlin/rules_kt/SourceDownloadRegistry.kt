package rules_kt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import org.icpclive.cds.api.MediaType
import org.icpclive.cds.api.RunInfo
import java.util.concurrent.ConcurrentHashMap


fun interface SourceLoader {
    fun load(): String
}

object SourceDownloadRegistry {
    data class SourceKey(val runId: Int, val urls: String)

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val downloads = ConcurrentHashMap<SourceKey, Deferred<SourceLoader?>>()

    fun isInFlight(key: SourceKey): Boolean = downloads[key]?.isActive == true

    operator fun get(key: SourceKey): Deferred<SourceLoader?>? = downloads[key]

    fun ensureDownload(key: SourceKey, downloadBlock: suspend () -> SourceLoader?): Deferred<SourceLoader?> {
        return downloads.computeIfAbsent(key) {
            scope.async {
                downloadBlock()
            }
        }
    }

    fun keyFrom(runInfo: RunInfo): SourceKey? {
        val urls = runInfo.sourceFiles.mapNotNull {
            when (it) {
                is MediaType.ZipArchive -> it.url
                else -> null
            }
        }
        if (urls.isEmpty()) return null
        val joined = urls.joinToString("\n")
        return SourceKey(runInfo.id.value.toInt(), joined)
    }
} 