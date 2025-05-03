package katalyzeapp

import org.apache.logging.log4j.kotlin.logger
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.toProblemId
import org.icpclive.cds.api.toTeamId

object CommentaryEntityReferences {
    private val clicsProblemPattern = "\\{problem:([-_0-9a-zA-Z]+)}".toRegex()
    private val clicsTeamsPattern = "\\{team:([-_0-9a-zA-Z]+)}".toRegex()
    private val dbProblemPattern = "#p([a-zA-Z])".toRegex();
    private val dbTeamsPattern = "#t(\\d+)".toRegex();

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
        return dbCommentary.replace(dbProblemPattern) { matchResult ->
            val problemLetter = matchResult.groupValues[1]
            val problem = problems.values.find { it.displayName == problemLetter }
            if (problem != null) {
                "{problem:${problem.id}}"
            } else {
                LOGGER.warn("Problem '$problemLetter' not found: ${this.problems}")
                "problem $problemLetter"
            }
        }.replace(dbTeamsPattern) { matchResult ->
            val teamId = matchResult.groupValues[1]
            val team = teams[teamId.toTeamId()]
            if (team != null) {
                "{team:${team.id}}"
            } else {
                LOGGER.warn("Team '$teamId' not found: ${this.teams}")
                matchResult.value
            }
        }
    }


    private val LOGGER = logger()
}