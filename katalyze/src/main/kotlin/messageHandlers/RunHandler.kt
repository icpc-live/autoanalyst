package messageHandlers

import legacyfeed.SimpleMessage
import model.InitialSubmission
import model.Problem
import model.Team
import org.jfree.util.Log
import java.security.InvalidKeyException

class RunHandler : SingleMessageHandler("run") {
    private fun processFreshSubmission(message: SimpleMessage) {
        val secondsFromStart = message.getDouble("time")

        val minutesFromStart = (secondsFromStart / 60.0).toInt()
        val submissionId = message["id"]
        val problemId = message["problem"]
        val teamId = message["team"]
        val language = message["language"]

        val team: Team
        val problem: Problem
        try {
            team = contest!!.getTeam(teamId)
            problem = contest!!.getProblem(problemId)
        } catch (e: InvalidKeyException) {
            error(String.format("Unable to processLegacyFeed message %s. Reason: %s", message, e.message))
            return
        }

        val newSubmission =
            InitialSubmission(submissionId, team, problem, language, (minutesFromStart * 60000).toLong())

        team.freshSubmission(newSubmission)
    }

    private fun processFinalizedSubmission(message: SimpleMessage) {
        val secondsFromStart = message.getDouble("time")
        val millisFromStart = (secondsFromStart * 1000).toInt()

        val submissionId = message["id"]
        val problemId = message["problem"]
        val judgement = message["result"]
        val solved = message.getBool("solved")
        val penalty = message.getBool("penalty")
        val teamId = message["team"]
        val language = message["language"]
        val isJudged = message.getBool("judged")

        val team: Team
        val problem: Problem
        try {
            team = contest!!.getTeam(teamId)
            problem = contest!!.getProblem(problemId)
        } catch (e: InvalidKeyException) {
            error(String.format("Unable to processLegacyFeed message %s. Reason: %s", message, e.message))
            return
        }

        if (isJudged) {
            val submission = contest!!.analyzer.submissionById(submissionId)
            team.submit(submission, millisFromStart.toLong(), submissionId, problem, judgement, solved, penalty)
        } else {
            Log.info(
                String.format(
                    "%s judgement of %s has been judged. Outcome is not disclosed",
                    team, problem
                )
            )
        }
    }

    override fun process(message: SimpleMessage) {
        // for now, only consider solved submissions

        val status = message["status"]
        val judged = message["judged"]
        if ("fresh" == status || "False" == judged) {
            processFreshSubmission(message)
        }

        if ("done" == status || "True" == judged) {
            processFinalizedSubmission(message)
        }
    }
}