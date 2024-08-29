package rules_kt

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
