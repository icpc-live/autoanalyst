package rules_kt

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.Instant
import model.Commentary
import model.dsl.v1.Contests
import model.dsl.v1.Problems
import model.dsl.v1.TeamRegions
import model.dsl.v1.Teams
import org.icpclive.cds.api.*
import org.icpclive.cds.scoreboard.ContestStateWithScoreboard
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection.TRANSACTION_SERIALIZABLE
import kotlin.time.Duration

class ContestInfoDb(val db: Database, val contestId: String) : RuleInterface() {

    data class ContestEntry(
        val id: String,
        val contestName: String,
        val startTime: Instant,
        val isCountdownPaused: Boolean,
        val length: Duration,
        val freeze: Duration?,
    )

    override val filters = listOf(FlowFilters::isContestInfoUpdate)

    override suspend fun process(contestStateWithScoreboard: ContestStateWithScoreboard) = emptyFlow<Commentary>().also {
        val infoAfter = contestStateWithScoreboard.state.infoAfterEvent!!
        val infoBefore = contestStateWithScoreboard.state.infoBeforeEvent
        val contestAfter = infoAfter.toEntry(contestId)
        if (contestAfter != infoBefore?.toEntry(contestId)) {
            upsertContest(contestAfter)
        }
        if (infoAfter.problems != infoBefore?.problems) {
            upsertProblems(infoAfter.problems.values.toList())
        }
        if (infoAfter.teams != infoBefore?.teams || infoAfter.organizations != infoBefore.organizations || infoAfter.groups != infoBefore.groups) {
            upsertTeams(infoAfter.teams.values.toList(), infoAfter.organizations, infoAfter.groups)
        }
    }

    private fun upsertContest(entry: ContestEntry) {
        transaction(db = db, transactionIsolation = TRANSACTION_SERIALIZABLE) {
            maxAttempts = 5
            addLogger(StdOutSqlLogger)
            Contests.upsert {
                it[id] = entry.id
                it[contestName] = entry.contestName
                it[startTime] = entry.startTime.intEpochSeconds
                it[isCountdownPaused] = entry.isCountdownPaused
                it[length] = entry.length.inWholeSeconds.toInt()
                it[freeze] = entry.freeze?.inWholeSeconds?.toInt()
            }
        }
    }

    private fun upsertProblems(entries: List<ProblemInfo>) {
        transaction(db = db, transactionIsolation = TRANSACTION_SERIALIZABLE) {
            maxAttempts = 5
            addLogger(StdOutSqlLogger)
            Problems.deleteAll()
            entries.forEach { entry ->
                Problems.insert {
                    it[problemId] = entry.displayName
                    it[problemName] = entry.fullName
                    it[color] = entry.color?.value
                }
            }
        }
    }

    private fun upsertTeams(
        entries: List<TeamInfo>, organizations: Map<OrganizationId, OrganizationInfo>, groups: Map<GroupId, GroupInfo>
    ) {
        transaction(db = db, transactionIsolation = TRANSACTION_SERIALIZABLE) {
            maxAttempts = 5
            addLogger(StdOutSqlLogger)
            Teams.deleteAll()
            TeamRegions.deleteAll()
            entries.forEach { entry ->
                val organization = organizations[entry.organizationId]
                Teams.insert {
                    it[id] = entry.id.value.toInt()
                    it[teamId] = entry.id.value.toInt()
                    it[teamName] = entry.displayName
                    it[institutionId] = organization?.id?.value?.toInt()
                    it[schoolName] = organization?.fullName
                    it[schoolShort] = organization?.displayName
                    it[country] = null // TODO
                }
                entry.groups.map { groups[it]!! }.forEach { group ->
                    TeamRegions.insert {
                        it[teamId] = entry.id.value.toInt()
                        it[regionId] = group.id.value.toInt()
                        it[regionName] = group.displayName
                        it[superRegionId] = group.id.value.toInt()
                        it[superRegionName] = group.displayName
                    }
                }
            }
        }
    }

    private fun ContestInfo.toEntry(id: String): ContestEntry {
        return ContestEntry(
            id = id,
            contestName = name,
            startTime = (startTime ?: Instant.DISTANT_FUTURE),
            isCountdownPaused = ((status as? ContestStatus.BEFORE)?.holdTime != null),
            length = contestLength,
            freeze = freezeTime
        )
    }

    private val Instant.intEpochSeconds: Int
        get() {
            return epochSeconds.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
        }

}