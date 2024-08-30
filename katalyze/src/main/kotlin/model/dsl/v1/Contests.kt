package model.dsl.v1

import org.jetbrains.exposed.sql.Table

object Contests: Table() {
    val id = varchar("id", 36)
    val contestName = varchar("contest_name", 150)
    val startTime = integer("start_time")  // Contest start time as Unix Epoch seconds
    val isCountdownPaused = bool("is_countdown_paused").default(false)
    val length = integer("length")  // Contest length in seconds
    val freeze = integer("freeze").nullable()  // Seconds into contest when scoreboard is frozen. TODO: remove

    override val primaryKey = PrimaryKey(id)
}