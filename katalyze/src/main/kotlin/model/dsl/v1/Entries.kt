package model.dsl.v1

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.*
import kotlin.time.*

object Entries: Table(name="entries") {
    val id = integer("id").autoIncrement()
    // TODO: it looks like exposed is unaware about kotlinx.datetime.Instant migration
    val date = timestamp("date")
        .defaultExpression(CurrentTimestamp)
        .transform(
            { it.toKotlinInstant() },
            { it.toJavaInstant() }
        )
    val contestTime = integer("contest_time")
    val priority = integer("priority")
    val user = varchar("user", 10)
    val text = text("text")
    val submissionId = integer("submission_id").nullable()
    override val primaryKey = PrimaryKey(id)
}
