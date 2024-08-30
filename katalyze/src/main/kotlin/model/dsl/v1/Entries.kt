package model.dsl.v1

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Entries: Table() {
    val id = integer("id").autoIncrement()
    val date = timestamp("date").defaultExpression(CurrentTimestamp)
    val contestTime = integer("contest_time")
    val priority = integer("priority")
    val user = varchar("user", 10)
    val text = text("text")
    val submissionId = integer("submission_id").nullable()
    override val primaryKey = PrimaryKey(id)
}
