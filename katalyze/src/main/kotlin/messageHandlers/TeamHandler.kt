package messageHandlers

import legacyfeed.SimpleMessage
import model.Team
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.sql.Connection
import java.sql.SQLException

class TeamHandler(private val connection: Connection?) : SingleMessageHandler("team") {
    override fun process(message: SimpleMessage) {
        val teamNumber = message["id"]
        val teamName = message["name"]
        var shortName: String? = getTeamShortName(teamNumber)
        if (StringUtils.isEmpty(shortName)) {
            shortName = teamName
        }
        val newTeam = Team(
            contest, teamNumber, teamName, shortName, null, arrayOfNulls(0),
            arrayOfNulls(0), arrayOfNulls(0), false
        )
        contest!!.addTeam(newTeam)
    }

    private fun getTeamShortName(teamId: String): String {
        if (connection == null) {
            return ""
        }

        try {
            val teamNumber = teamId.toInt()

            val s = connection.prepareStatement("select school_short from teams where team_id = ?")
            s.setInt(1, teamNumber)
            val results = s.executeQuery()

            if (results.next()) {
                return results.getString("school_short")
            } else {
                logger.warn(String.format("Team with id %s not in database", teamId))
                return ""
            }
        } catch (e: NumberFormatException) {
            return ""
        } catch (e: SQLException) {
            logger.error(String.format("Unable to retrieve team id %d information", e.message))
            return ""
        }
    }

    companion object {
        var logger: Logger = LogManager.getLogger(TeamHandler::class.java)
    }
}