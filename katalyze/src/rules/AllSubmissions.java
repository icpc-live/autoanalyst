package rules;

import org.apache.log4j.Logger;
import model.Submission;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import katalyzeapp.DatabaseNotificationConfig;


public class AllSubmissions implements StandingsUpdatedEvent {
    static Logger logger = Logger.getLogger(AllSubmissions.class);

    Connection conn = null;

	public AllSubmissions(DatabaseNotificationConfig dbConfig) throws Exception{
		conn = dbConfig.createConnection();
	}

    public void onStandingsUpdated(StandingsTransition transition) {
        Submission submission = transition.submission;

        try {
            PreparedStatement s;
            s = conn.prepareStatement("insert into submissions (problem_id, team_id, lang_id, result, date, contest_time, submission_id) values (?, ?, ?, ?, ?, ?, ?)");
            s.setString(1, submission.getProblem().getLetter());
            s.setInt(2, submission.getTeam().getTeamNumber());
            s.setString(3, submission.getLanguage());
            s.setString(4, submission.getOutcome());
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = new Date();
            s.setString(5, df.format(d));
            s.setInt(6, submission.getMinutesFromStart());
            s.setInt(7, submission.getInitialSubmission().id.intValue());

            s.executeUpdate();

            logger.debug("inserted into db: " + s);
        } catch (Exception e) {
            logger.error("exception in sql insert: " + e.getMessage());
        }
    }
}
