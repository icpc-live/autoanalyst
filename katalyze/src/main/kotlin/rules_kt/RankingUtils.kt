package rules_kt

import kotlinx.collections.immutable.toImmutableList
import model.Contest
import org.icpclive.cds.api.*
import org.icpclive.cds.scoreboard.Ranking


fun Ranking.getTeamRank(teamId: TeamId): Int {
    return order.zip(ranks).first { (t, _) -> teamId == t  }.second
}

fun rankString(current: Int, previous: Int): String {
    return if (current == 1) {
        if (previous == 1) {
            "still leading the competition"
        } else {
            "now leading the competition"
        }
    } else if (current == 2) {
        if (previous == 2) {
            "still the runner-up"
        } else {
            "now the runner-up"
        }
    } else {
        "at rank $current"
    }
}

fun futureRankString(current: Int, previous: Int): String {
    return if (current == 1) {
        if (previous == 1) {
            "further extend their lead"
        } else {
            "lead the competition"
        }
    } else if (current == 2) {
        if (previous == 2) {
            "still be the runner-up"
        } else {
            "become the runner-up"
        }
    } else {
        "get rank $current"
    }
}

fun Ranking.rankTags(prefix: String, teamId: TeamId): List<String> {
    require(prefix in setOf("submission", "accepted"))
    val result = mutableListOf(prefix)
    for (award in awards) {
        if (teamId in award.teams) {
            when (award) {
                is Award.Medal -> {
                    result.add("$prefix-medal")
                    award.medalColor?.let { color -> result.add("$prefix-medal-${color.name.lowercase()}") }
                }
                is Award.Winner -> {
                    result.add("$prefix-winner")
                }
                else -> {}
            }
        }
    }
    return result.toImmutableList()
}

fun ContestInfo.getScoreboardProblemIndex(problemId: ProblemId): Int? {
    val problemIndex = scoreboardProblems.indexOfFirst { problemInfo -> problemInfo.id == problemId }
    if (problemIndex == -1) return null
    return problemIndex
}

fun ScoreboardRow.getResultByProblemId(problemId: ProblemId, info: ContestInfo): ICPCProblemResult? {
    val problemIndex = info.getScoreboardProblemIndex(problemId)?:return null
    return problemResults[problemIndex] as? ICPCProblemResult
}

fun RunInfo.isAccepted(): Boolean {
    return (result as? RunResult.ICPC)?.verdict?.isAccepted == true
}
