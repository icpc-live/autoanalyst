package katalyzeapp

import katalyzeapp.CommentaryEntityReferences.asDBCommentary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import model.Commentary
import model.dsl.v1.Entries
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

suspend fun streamCommentaryToDB(
    db: Database,
    contestStateTracker: ContestStateTracker,
    commentaryFlow: Flow<Commentary>
) {
    var startFromTime: Duration? = null
    transaction(db) {
        // select maximum contest time of entries where user is katalyzer
        Entries.deleteWhere{ user eq Commentary.KATALYZER_USER }
        //val maxContestTime = Entries.contestTime.max()
        //val row = Entries.select(maxContestTime).where(Entries.user eq Commentary.KATALYZER_USER).firstOrNull()
        //startFromTime = row?.get(maxContestTime)?.minutes
        //if (startFromTime != null) {
        //    println("Starting from $startFromTime")
        //}
    }
    commentaryFlow.filter { entry ->
        entry.isAutomatic && entry.contestTime >= (startFromTime?: ZERO)
    }.collect { entry ->
        transaction(db) {
            Entries.upsert { row ->
                row[user] = Commentary.KATALYZER_USER
                row[contestTime] = entry.contestTime.inWholeMinutes.toInt()
                row[priority] = entry.importance.ordinal
                row[text] = contestStateTracker.state!!.info.asDBCommentary(entry.message)
                row[submissionId] = entry.submissionIds?.firstOrNull()?.toInt()
            }
        }
    }
}
