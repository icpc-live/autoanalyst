package model.dsl.v1

import org.jetbrains.exposed.v1.core.Table


object SubmissionSources : Table(name = "submission_sources") {
    val id = integer("id").autoIncrement()
    val runId = integer("run_id")
    val sourceUrls = text("source_urls")
    val content = text("content")

    init {
        uniqueIndex("avoid_dups_sources", runId, sourceUrls)
    }

    override val primaryKey = PrimaryKey(id)
} 