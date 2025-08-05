package model.dsl.v1

import org.jetbrains.exposed.v1.core.Table


object TeamRegions : Table("team_regions") {
    val id = integer("id").autoIncrement()
    val regionId = integer("region_id")
    val teamId = integer("team_id")
    val regionName = varchar("region_name", 100)
    val superRegionName = varchar("super_region_name", 100)
    val superRegionId = integer("super_region_id")

    override val primaryKey = PrimaryKey(id)
}