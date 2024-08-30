package model.dsl.v1

import org.jetbrains.exposed.sql.Table


object Problems: Table() {
    val id = integer("id").autoIncrement()
    val problemId = varchar("problem_id", 10).uniqueIndex()
    val problemName = varchar("problem_name", 255)
    val color = varchar("color", 10).nullable()

    override val primaryKey = PrimaryKey(id)
}