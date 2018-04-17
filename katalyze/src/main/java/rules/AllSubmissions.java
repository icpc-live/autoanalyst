package rules;

import model.InitialSubmission;
import org.apache.log4j.Logger;
import model.Judgement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import katalyzeapp.DatabaseNotificationConfig;


public class AllSubmissions implements StandingsUpdatedEvent {
    private static Logger logger = Logger.getLogger(AllSubmissions.class);
    private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private final Connection conn;

	public AllSubmissions(DatabaseNotificationConfig dbConfig) throws Exception{
		conn = dbConfig.createConnection();
	}

    public void onStandingsUpdated(StandingsTransition transition) {
        Judgement submission = transition.judgement;
        InitialSubmission initialSubmission = submission.getInitialSubmission();

        try {
            PreparedStatement s;
            s = conn.prepareStatement("insert into submissions (problem_id, team_id, lang_id, result, date, contest_time, submission_id) values (?, ?, ?, ?, ?, ?, ?)");
            s.setString(1, submission.getProblem().getLabel());
            s.setString(2, submission.getTeam().getId());
            s.setString(3, initialSubmission.getLanguage());
            s.setString(4, submission.getOutcome());
            s.setString(5, df.format(new Date()));
            s.setInt(6, submission.getInitialSubmission().minutesFromStart);
            s.setString(7, initialSubmission.id);

            s.executeUpdate();

            logger.debug("inserted into db: " + s);
        } catch (Exception e) {
        	String message = e.getMessage();
        	String logMessage = "Error adding judgement to database: "+message;
        	
        	if (message.contains("Duplicate entry")) {
        		logger.debug(logMessage);
        	} else {
        		logger.error(logMessage);
        	}
        }
    }
}
