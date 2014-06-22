package icat;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AnalystMessage {
	public final int id;
	public final Date date;
	public final int contestTime;
	public final int priority;
	public final String user;
	public final int submissionId;
	public final String text;

	public AnalystMessage(int id, Date date, int contestTime, int priority, String user, int submissionId, String text) {
		this.id = id;
		this.date = date;
		this.contestTime = contestTime;
		this.priority = priority;
		this.user = user;
		this.submissionId = submissionId;
		this.text = text;
	}
	
	public static AnalystMessage fromSQL(ResultSet s) throws SQLException {
		int submissionId = s.getInt("submission_id");
		if (s.wasNull()) {
			submissionId = -1;
		}
		return new AnalystMessage(
				s.getInt("id"),
				s.getDate("date"),
				s.getInt("contest_time"),
				s.getInt("priority"),
				s.getString("user"),
				submissionId,
				s.getString("text"));				
	}
	
}
