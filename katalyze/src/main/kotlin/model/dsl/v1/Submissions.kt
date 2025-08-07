package model.dsl.v1

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp
import kotlin.time.*

object Submissions : Table(name="submissions") {
    val id = integer("id").autoIncrement()
    val problemId = varchar("problem_id", 10)
    val teamId = integer("team_id")
    val langId = varchar("lang_id", 11)
    val result = varchar("result", 10)
    val date = timestamp("date").transform(
        { it.toKotlinInstant() },
        { it.toJavaInstant() }
    ).clientDefault { Clock.System.now() }
    val contestTime = integer("contest_time")
    val submissionId = integer("submission_id").uniqueIndex("avoid_dups")

    override val primaryKey = PrimaryKey(id)
}
