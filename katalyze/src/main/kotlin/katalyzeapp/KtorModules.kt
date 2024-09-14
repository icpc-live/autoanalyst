package katalyzeapp

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.json.Json
import model.Commentary
import web.WebPublisher

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
private val jsonFlowBuilderContext = newSingleThreadContext("JsonFlowBuilder")

fun Application.commentaryMessagesModule(commentaryFlow: SharedFlow<Commentary>) {
    val coroutineScope = CoroutineScope(jsonFlowBuilderContext)
    val jsonFlow = commentaryFlow.map {
        Json.encodeToString(
            org.icpclive.clics.v202306.objects.Commentary.serializer(),
            org.icpclive.clics.v202306.objects.Commentary(
                id = it.id,
                contestTime = it.contestTime,
                message = it.message,
                time = it.time,
                teamIds = it.teamIds,
                submissionIds = it.submissionIds,
                tags = it.tags + listOf("importance-${it.importance.name.lowercase()}"),
            )
        )
    }.shareIn(coroutineScope, SharingStarted.Eagerly, Int.MAX_VALUE)
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
    }
    environment.monitor.subscribe(ApplicationStopping) {
        coroutineScope.cancel()
    }
}

fun Application.scoreboardPublisherModule(contestStateTracker: ContestStateTracker) {
    val webPublisher = WebPublisher(false)
    val scoreboardPublisher = ScoreboardPublisher(webPublisher, contestStateTracker)
    scoreboardPublisher.start()
    routing {
        get(Regex("/(scoreboard|teams)")) {
            val doc = webPublisher.get(call.request.path())
            if (doc == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respondOutputStream(ContentType.Application.Json) {
                    doc.writeContents(this)
                }
            }
        }
    }
    environment.monitor.subscribe(ApplicationStopping) {
        runBlocking { scoreboardPublisher.stop() }
    }
}