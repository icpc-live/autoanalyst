package model.dsl.v1

import org.jetbrains.exposed.sql.Table

object Teams : Table() {
    val id = integer("id").autoIncrement()
    val teamId = integer("team_id")
    val teamName = varchar("team_name", 150)
    val institutionId = integer("institution_id").nullable()
    val schoolName = varchar("school_name", 150).nullable()
    val schoolShort = varchar("school_short", 32).nullable()
    val country = varchar("country", 50).nullable()

    override val primaryKey = PrimaryKey(id)
}