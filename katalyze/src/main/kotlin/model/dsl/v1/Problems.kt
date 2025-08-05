package model.dsl.v1

import org.jetbrains.exposed.v1.core.Table


object Problems: Table(name="problems") {
    val id = integer("id").autoIncrement()
    val problemId = varchar("problem_id", 10).uniqueIndex()
    val problemName = varchar("problem_name", 255)
    val color = varchar("color", 10).nullable()

    override val primaryKey = PrimaryKey(id)
}
