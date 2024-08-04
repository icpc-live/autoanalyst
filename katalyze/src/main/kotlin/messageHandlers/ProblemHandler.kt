package messageHandlers

import legacyfeed.SimpleMessage
import model.Problem
import org.apache.logging.log4j.kotlin.logger

class ProblemHandler : SingleMessageHandler("problem") {
    override fun process(message: SimpleMessage) {
        val id = message["id"]

        val abbrev = getProblemAbbreviation(id)

        val problemName = message["name"].trim { it <= ' ' }
        logger.info("addProblem(" + message["id"] + ", " + id + ", " + abbrev + ", " + problemName + ")")
        val newProblem = Problem(id, problemName, abbrev, null)
        contest!!.addProblem(newProblem)
    }

    companion object {
        private val logger = logger()

        private fun getProblemAbbreviation(id: String): String {
            try {
                val idAsInteger = id.toInt()
                return "" + Character.toChars(64 + idAsInteger)[0]
            } catch (e: NumberFormatException) {
                return id
            }
        }
    }
}