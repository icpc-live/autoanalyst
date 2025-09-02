package model.dsl.v1

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.*
import kotlin.time.*

//team_id, path, modify_timestamp, modify_time, file_size_bytes, line_count, lines_changed, git_tag 
object EditActivity: Table(name="edit_activity") {
    val id = integer("id").autoIncrement()
    val teamId = integer("team_id")
    val path = text("path")
    val modifyTimestamp = integer("modify_timestamp")
    val modifyTime = integer("modify_time")
    val lineCount = integer("line_count")
    val fileSizeBytes = integer("file_size_bytes")
    val linesChanged = integer("lines_changed")
    val gitTag = varchar("git_tag", 30)

    override val primaryKey = PrimaryKey(id)
}
