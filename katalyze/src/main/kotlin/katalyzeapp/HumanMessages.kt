package katalyzeapp

import katalyzeapp.CommentaryEntityReferences.asClicsCommentary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.whileSelect
import kotlinx.datetime.Clock
import model.Commentary
import model.EventImportance
import model.dsl.v1.Entries
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.toTeamId
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
fun getHumanMessagesFromDatabase(
    scope: CoroutineScope,
    db: Database,
    contestStateTracker: ContestStateTracker
): ReceiveChannel<Commentary> =
    scope.produce(capacity = UNLIMITED) {
        val seenIds = mutableSetOf<Int>();
        while (true) {
            val contestInfo = contestStateTracker.state?.info
            if (contestInfo == null) {
                kotlinx.coroutines.delay(1.seconds)
                continue
            }
            val newEntries = transaction(db) {
                val entries =
                    Entries.selectAll().where(Entries.user.neq(Commentary.KATALYZER_USER)).orderBy(Entries.date).toList()
                entries.filter { entry ->
                    (entry[Entries.id] !in seenIds).also {
                        if (it) {
                            seenIds.add(entry[Entries.id])
                        }
                    }
                }
            }
            newEntries.forEach { entry -> send(commentaryFromDBRow(entry, contestInfo)) }
            kotlinx.coroutines.delay(2.seconds)
        }
    }

private fun commentaryFromDBRow(row: ResultRow, info: ContestInfo): Commentary {
    val message = info.asClicsCommentary(row[Entries.text])
    return Commentary(
        time = row[Entries.date],
        contestTime = row[Entries.contestTime].minutes,
        isAutomatic = false,
        message = message,
        problemIds = emptyList(),  // TODO: fix
        teamIds = emptyList(),  // TODO: fix
        importance = if (row[Entries.priority] == 0) EventImportance.Breaking else EventImportance.AnalystMessage,
        tags = listOf("human"),
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
fun mergeCommentaryChannelsByContestTime(
    scope: CoroutineScope,
    vararg channels: ReceiveChannel<Commentary>
): ReceiveChannel<Commentary> =
    scope.produce {
        val initialCommentaries = mutableListOf<Commentary>()
        whileSelect { // Selects the next element from one of the channels
            channels.forEach { channel ->
                channel.onReceiveCatching { commentary ->
                    if (commentary.isClosed) {
                        false
                    } else {
                        initialCommentaries.add(commentary.getOrThrow())
                        true
                    }
                }
            }
            onTimeout(500.milliseconds) {
                initialCommentaries.isEmpty()
            }
        }
        initialCommentaries.sortBy { it.contestTime }
        initialCommentaries.forEach { send(it) }
        initialCommentaries.clear()
        // After the initial warm-up just merge the channels normally
        whileSelect { // Selects the next element from one of the channels
            channels.forEach { channel ->
                channel.onReceiveCatching { commentary ->
                    if (commentary.isClosed) {
                        false
                    } else {
                        send(commentary.getOrThrow())
                        true
                    }
                }
            }
        }
    }