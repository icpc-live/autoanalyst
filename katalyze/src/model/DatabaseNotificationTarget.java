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
	
	private void updateScoreboard(LoggableEvent event) {
		// this method replaces EVERYTHING in the scoreboard row for
		// the given team
		int teamId = event.team.getTeamNumber();
		Score score = event.score;
		
		int totalTime = score.getTimeIncludingPenalty();
		int numSolutions = score.solvedProblemCount();
		
		// build up all the changed fields
		String changes = "total_time = ?, num_solutions = ?";
		int numProblems = event.contest.getProblems().size();
		for (Problem p : event.contest.getProblems()) {
			changes = changes + ", " + p.getLetter().toLowerCase() + "_submissions = ?";
			changes = changes + ", " + p.getLetter().toLowerCase() + "_soln_time = ?";
		}

		try {
			String query = "UPDATE scoreboard SET " + changes + " WHERE team_id = ?";
			PreparedStatement s = conn.prepareStatement(query);

			// populate the fields
			s.setInt(1, event.score.getTimeIncludingPenalty()); // total time (including penalties)
			s.setInt(2, event.score.solvedProblemCount()); // total # solved problems
			int field = 3;
			int solutionCountByTime = 0; // TEST
			int solutionCount = 0; // TEST
			for (Problem p : event.contest.getProblems()) {
				if (event.score.solutionTime(p) > 0) { solutionCountByTime++; } // TEST
				if (event.score.isSolved(p)) { solutionCount++; } // TEST
				s.setInt(field++, event.score.submissionCount(p));
				s.setInt(field++, event.score.solutionTime(p));
			}
			s.setInt(field, teamId);
 
			logger.info("solution count: " + solutionCount + ", by time: " + solutionCountByTime + ", solvedProblemCount: " + event.score.solvedProblemCount()); // TEST

			logger.info("updating scoreboard: " + s);

			s.executeUpdate();
		} catch (Exception e) {
			logger.error("exception in updating scoreboard: " + e.getMessage());
		}
		
	}

	@Override
	public void notify(LoggableEvent event) {
		logger.info(String.format("[%d] %s", event.time, event.message));
		
		updateScoreboard(event);		

		try {
			PreparedStatement s;
			s = conn.prepareStatement("insert into entries (contest_time, user, text, priority) values (?, ?, ?, ?)");
			s.setInt(1, event.time);
			s.setString(2, "katalyzer");
			s.setString(3, event.icatMessage);
			s.setInt(4, event.importance.ordinal());

			s.executeUpdate();

			logger.info("inserted into db: " + s);
		} catch (Exception e) {
			logger.error("exception in sql insert: " + e.getMessage());
		}
	}
}
