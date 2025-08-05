package model.dsl.v1

import org.jetbrains.exposed.v1.core.Table
import kotlin.time.Clock
import kotlin.time.Instant

object Submissions : Table(name="submissions") {
    val id = integer("id").autoIncrement()
    val problemId = varchar("problem_id", 10)
    val teamId = integer("team_id")
    val langId = varchar("lang_id", 11)
    val result = varchar("result", 10)
    // TODO: it looks like exposed is unaware about kotlinx.datetime.Instant migration
    val date = long("date").transform(
        { Instant.fromEpochMilliseconds(it) },
        { it.toEpochMilliseconds() }
    ).clientDefault { Clock.System.now() }
    val contestTime = integer("contest_time")
    val submissionId = integer("submission_id").uniqueIndex("avoid_dups")

    override val primaryKey = PrimaryKey(id)
}
