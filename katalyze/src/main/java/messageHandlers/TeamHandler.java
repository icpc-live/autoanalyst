package messageHandlers;

import legacyfeed.SimpleMessage;
import model.Team;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TeamHandler extends SingleMessageHandler {
	static Logger logger = Logger.getLogger(TeamHandler.class);

	private final Connection connection;

	public TeamHandler(Connection connection) {
		super("team");
		this.connection = connection;
	}
	
	public void process(SimpleMessage message) {
		String teamNumber = message.get("id");
		String teamName = message.get("name");
		String shortName = getTeamShortName(teamNumber);
		if (StringUtils.isEmpty(shortName)) {
			shortName = teamName;
		}
		Team newTeam = new Team(contest, teamNumber, teamName, shortName, null);
		contest.addTeam(newTeam);
	}

	private String getTeamShortName(String teamId) {
		if (connection == null) {
			return "";
		}

		try {
			int teamNumber = Integer.parseInt(teamId);

			PreparedStatement s = connection.prepareStatement("select school_short from teams where team_id = ?");
			s.setInt(1, teamNumber);
			ResultSet results = s.executeQuery();

			if (results.next()) {
				return results.getString("school_short");
			} else {
				logger.warn(String.format("Team with id %s not in database", teamId));
				return "";
			}
		} catch (NumberFormatException e) {
			return "";
		}
		catch (SQLException e) {
			logger.error(String.format("Unable to retrieve team id %d information", e.getMessage()));
			return "";
		}
	}
	
}
