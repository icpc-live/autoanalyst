package katalyzeapp

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.withIndex
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import model.Commentary
import org.icpclive.clics.FeedVersion
import org.icpclive.clics.clicsEventsSerializersModule
import org.icpclive.clics.events.Event
import org.icpclive.clics.events.EventToken
import org.icpclive.clics.v202306.objects.Commentary as ClicsCommentary
import org.icpclive.clics.v202306.events.CommentaryEvent
import web.WebPublisher

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
private val jsonFlowBuilderContext = newSingleThreadContext("JsonFlowBuilder")

@OptIn(ExperimentalSerializationApi::class)
fun Application.commentaryMessagesModule(commentaryFlow: SharedFlow<Commentary>) {
    val coroutineScope = CoroutineScope(jsonFlowBuilderContext)
    val jsonFlow = commentaryFlow.map {
        ClicsCommentary(
            id = it.id,
            contestTime = it.contestTime,
            message = it.message,
            time = it.time,
            teamIds = it.teamIds,
            submissionIds = it.submissionIds,
            tags = it.tags + listOf("importance-${it.importance.name.lowercase()}"),
        )
    }.shareIn(coroutineScope, SharingStarted.Eagerly, Int.MAX_VALUE)
    val commentsMapFlow = jsonFlow.runningFold(persistentMapOf<String, ClicsCommentary>()) { acc, value ->
        acc.put(value.id, value)
    }.stateIn(coroutineScope, SharingStarted.Eagerly, persistentMapOf())
    val json = Json {
        serializersModule = clicsEventsSerializersModule(
            FeedVersion.`2023_06`,
            tokenPrefix = ""
        )
    }
    routing {
        route("/api/contests/contest") {
            get("/commentary/{id}") {
                val comment = commentsMapFlow.value[call.parameters["id"]]
                if (comment == null) {
                    call.respond(HttpStatusCode.NotFound, "comment ${call.parameters["id"]} not found")
                } else {
                    call.respond(comment)
                }
            }
            get("/commentary") {
                call.respond(commentsMapFlow.value.values.toList())
            }
            get("/event-feed") {
                call.respondOutputStream(
                    contentType = ContentType(
                        "application",
                        "x-ndjson",
                        listOf(HeaderValueParam("charset", "utf-8"))
                    )
                ) {
                    jsonFlow.withIndex().collect { (index, item) ->
                        json.encodeToStream(
                            CommentaryEvent(item.id, EventToken("cds-${index}"), item) as Event,
                            this@respondOutputStream
                        )
                        write("\n".encodeToByteArray())
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