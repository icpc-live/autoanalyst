package model;

import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;

import katalyzeapp.DatabaseNotificationConfig;

public class DatabaseNotificationTarget implements NotificationTarget {
	
	static Logger logger = Logger.getLogger(DatabaseNotificationTarget.class);

	Connection conn;
	DatabaseNotificationConfig config;

	public DatabaseNotificationTarget(DatabaseNotificationConfig config) throws Exception {
		this.config = config;
		this.conn = config.createConnection();
	}
	
			
	@Override
	public void notify(LoggableEvent event) {
		logger.info(String.format("[%d] %s", event.time, event.message));

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

			logger.info("inserted into db: " + s);
		} catch (Exception e) {
			logger.error("exception in sql insert: " + e.getMessage());
		}
	}
}
