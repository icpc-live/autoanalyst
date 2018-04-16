package io;

import model.*;
import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import katalyzeapp.DatabaseNotificationConfig;

public class DatabaseNotificationTarget implements NotificationTarget, EntityChangedHandler {
	
	static Logger logger = Logger.getLogger(DatabaseNotificationTarget.class);

	Connection conn;
	DatabaseNotificationConfig config;
	int suppressedMinutes = 0;

	public DatabaseNotificationTarget(DatabaseNotificationConfig config) throws Exception {
		this.config = config;
		this.conn = config.createConnection();
	}
	
	public void suppressUntil(int contestMinutes) {
		this.suppressedMinutes = contestMinutes;
	}
	
			
	@Override
	public void notify(LoggableEvent event) {
		if (event.contestTimeMinutes() < suppressedMinutes) {
			logger.info("skipping message (due to restart): " + event.icatMessage);
			return;
		}

		// If the event already came from the database, don't write it back again.
		if (event.supplements != null) {
			String category = event.supplements.get("category");
			if ("human".equals(category)) {
				return;
			}
		}

		try {
			PreparedStatement s;
			s = conn.prepareStatement("insert into entries (contest_time, user, text, priority, submission_id) values (?, ?, ?, ?, ?)");
			s.setInt(1, event.contestTimeMinutes());
			s.setString(2, "katalyzer");
			s.setString(3, event.icatMessage);
			s.setInt(4, event.importance.ordinal());
			if (event.submission != null) {
				s.setString(5, event.submission.id);
			} else {
				s.setNull(5, java.sql.Types.INTEGER);
			}

			s.executeUpdate();

			logger.debug("inserted into db: " + s);
		} catch (Exception e) {
			String errorMessage = e.getMessage();
			String logMessage = "Error adding message to entries table: "+errorMessage;
			
			if (errorMessage.contains("Duplicate entry")) {
				logger.debug(logMessage);
			} else {
				logger.error(logMessage);
			}
		}
	}

	public void entityChanged(ApiEntity entity, EntityOperation op) {
		try {
			if (entity instanceof Team) {
				teamChanged((Team) entity, op);
			} else if (entity instanceof Problem) {
				problemChanged((Problem) entity, op);
			} else if (entity instanceof ContestProperties) {
				contestChanged((ContestProperties) entity, op);
			}
		}  catch (Exception e) {
			String errorMessage = e.getMessage();
			String logMessage = String.format("Error handling operation %s of entity %s to teams table: %s", op.toString(), entity, errorMessage);

			if (errorMessage.contains("Duplicate entry")) {
				logger.debug(logMessage);
			} else {
				logger.error(logMessage);
			}
		}

	}

	private void teamChanged(Team team, EntityOperation op) throws Exception {
		Organization org = team.getOrganization();
		if (op == EntityOperation.CREATE) {
			PreparedStatement s;
			s = conn.prepareStatement("insert into teams (id, team_id, team_name, institution_id, school_name, school_short, country) values (?,?,?,?,?,?,?)");
			s.setString(1, team.getId());
			s.setString(2, team.getId());
			s.setString(3, team.getName());
			s.setString(4, org.getId());
			s.setString(5, org.getFullName());
			s.setString(6, org.getDisplayName());
			s.setString(7, org.getCountry());
			s.executeUpdate();
		}
	}
/*
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `contest_name` varchar(150) NOT NULL,
  `start_time` int(11) DEFAULT NULL COMMENT 'Contest start time as Unix Epoch seconds.',
  `length` int(11) DEFAULT NULL COMMENT 'Contest length in seconds.',
  `freeze` int(11) DEFAULT NULL COMMENT 'Seconds into contest when scoreboard is frozen.',
*/

	private void contestChanged(ContestProperties properties, EntityOperation op) throws Exception {

 		PreparedStatement s = conn.prepareStatement(
				"replace into contests(id, contest_name, start_time, length, freeze) values (?,?,?,?,?)");
 		s.setInt(1,1);
 		s.setString(2, properties.getName());
 		s.setInt(3, (int) properties.getStartTimeEpochSeconds());
 		s.setInt(4, (int) (properties.getDurationMillis() / 1000));

 		long freezeContestTime = (properties.getDurationMillis() - properties.getScoreboardFreezeMillis());
 		s.setInt(5, (int) (freezeContestTime/1000));
 		s.executeUpdate();

	}

	/*
	  `id` int(11) NOT NULL AUTO_INCREMENT,
  `problem_id` varchar(10) NOT NULL COMMENT 'The label (typically a single letter) within the contest.',
  `problem_name` varchar(255) NOT NULL,
  `color` varchar(10) DEFAULT NULL COMMENT 'Hex RGB color specification of the problem.',
	 */

	private void problemChanged(Problem problem, EntityOperation op) throws Exception {
		if (op == EntityOperation.CREATE) {
			PreparedStatement s = conn.prepareStatement("insert into problems (problem_id, problem_name, color) values(?,?,?)");
			s.setString(1, problem.getLabel());
			s.setString(2, problem.getName());
			s.setString(3, problem.getColor());
			s.executeUpdate();
		}

	}


}
