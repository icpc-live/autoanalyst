package katalyzeapp

import model.dsl.v1.Entries
import org.apache.logging.log4j.kotlin.logger
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.toProblemId
import org.icpclive.cds.api.toTeamId

object CommentaryEntityReferences {
    private val clicsProblemPattern = "\\{problem:(\\w+)}".toRegex()
    private val clicsTeamsPattern = "\\{team:(\\w+)}".toRegex()
    val problemPattern = "#p([a-zA-Z])".toRegex();
    val teamsPattern = "#t(\\d+)".toRegex();

    fun ContestInfo.asDBCommentary(clicsCommentary: String): String {
        return clicsCommentary.replace(clicsProblemPattern) {
            val problem = problems[it.groupValues[1].toProblemId()]
            if (problem == null) {
                LOGGER.error("Problem ${it.groupValues[1]} not found")
                it.value
            } else {
                "#p${problem.displayName}"
            }
        }.replace(clicsTeamsPattern) { matchResult ->
            val team = teams[matchResult.groupValues[1].toTeamId()]
            if (team == null) {
                LOGGER.error("Team ${matchResult.groupValues[1]} not found")
                matchResult.value
            } else {
                "#t${team.id}"
            }
        }
    }

    fun ContestInfo.asClicsCommentary(dbCommentary: String): String {
        return dbCommentary.replace(problemPattern) { matchResult ->
            val problemLetter = matchResult.groupValues[1]
            val problem = problems.values.find { it.displayName == problemLetter }
            if (problem != null) {
                "{problem:${problem.id}}"
            } else {
                "problem $problemLetter"
            }
        }.replace(teamsPattern) { matchResult ->
            val teamId = matchResult.groupValues[2]
            val team = teams[teamId.toTeamId()]
            if (team != null) {
                "{team:${team.id}}"
            } else {
                matchResult.value
            }
        }
    }


    private val LOGGER = logger()
}