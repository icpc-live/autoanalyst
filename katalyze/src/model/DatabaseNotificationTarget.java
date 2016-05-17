package model;

import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;

import katalyzeapp.DatabaseNotificationConfig;

public class DatabaseNotificationTarget implements NotificationTarget {
	
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
		if (event.time < suppressedMinutes) {
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
			s.setInt(1, event.time);
			s.setString(2, "katalyzer");
			s.setString(3, event.icatMessage);
			s.setInt(4, event.importance.ordinal());
			if (event.submission != null) {
				s.setInt(5, event.submission.id.intValue());
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


}
