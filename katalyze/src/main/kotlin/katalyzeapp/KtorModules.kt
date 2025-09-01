package katalyzeapp

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
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
import kotlinx.coroutines.flow.withIndex
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import model.Commentary
import model.dsl.v1.*
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.neq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.*
import org.icpclive.clics.FeedVersion
import org.icpclive.clics.clicsEventsSerializersModule
import org.icpclive.clics.events.Event
import org.icpclive.clics.events.EventToken
import org.icpclive.clics.objects.Commentary as ClicsCommentary
import org.icpclive.clics.events.CommentaryEvent
import web.WebPublisher
import kotlin.time.*

@OptIn(ExperimentalSerializationApi::class)
fun Application.commentaryMessagesModule(commentaryFlow: SharedFlow<Commentary>) {
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
    }.shareIn(this + Dispatchers.IO, SharingStarted.Eagerly, Int.MAX_VALUE)
    val commentsMapFlow = jsonFlow.runningFold(persistentMapOf<String, ClicsCommentary>()) { acc, value ->
        acc.put(value.id, value)
    }.stateIn(this + Dispatchers.IO, SharingStarted.Eagerly, persistentMapOf())
    val formatter = Json {
        serializersModule = clicsEventsSerializersModule(
            FeedVersion.`2023_06`,
            tokenPrefix = ""
        )
    }
    routing {
        install(ContentNegotiation) {
            json(formatter)
        }
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
                        formatter.encodeToStream(
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
}

fun Application.scoreboardPublisherModule(contestStateTracker: ContestStateTracker) {
    val webPublisher = WebPublisher(false)
    launch {
        ScoreboardPublisher(webPublisher, contestStateTracker).work()
    }
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
}

fun Application.editActivityModule(db: Database? = null) {
    routing {
        get("/editacticity/{id}") {
            if (db == null) {
                call.respond(HttpStatusCode.InternalServerError, "Database is not configured")
                return@get
            }
            val id = call.parameters["id"]?.toIntOrNull() ?: -1
            if (id == -1){
                call.respond(HttpStatusCode.BadRequest, "Invalid or missing id parameter")
                return@get
            }
            val edit_activity_rows = transaction(db) {
                val activities =
                    EditActivity.selectAll()
                    .where{EditActivity.teamId eq id}
                    .orderBy(EditActivity.modifyTimestamp)
                    .toList()
                activities
            }
            var last_modify = 0
            val res = mutableMapOf<String, Int>()
            edit_activity_rows.forEach{ entry ->
                if(entry[EditActivity.linesChanged] > 0){
                    val diff = entry[EditActivity.modifyTime] - last_modify
                    val cur = res[entry[EditActivity.path]] ?: 0
                    res[entry[EditActivity.path]] = cur + diff
                }
                last_modify = entry[EditActivity.modifyTime]
            }
            call.respondText(Json.encodeToString(res), ContentType.Application.Json)
        }
    }
}