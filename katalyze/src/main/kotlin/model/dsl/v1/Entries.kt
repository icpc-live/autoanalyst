package model.dsl.v1

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import kotlin.time.Instant

object Entries: Table(name="entries") {
    val id = integer("id").autoIncrement()
    // TODO: it looks like exposed is unaware about kotlinx.datetime.Instant migration
    val date = long("date").transform(
        { Instant.fromEpochMilliseconds(it) },
        { it.toEpochMilliseconds() }
    ).defaultExpression(CurrentTimestamp)
    val contestTime = integer("contest_time")
    val priority = integer("priority")
    val user = varchar("user", 10)
    val text = text("text")
    val submissionId = integer("submission_id").nullable()
    override val primaryKey = PrimaryKey(id)
}
