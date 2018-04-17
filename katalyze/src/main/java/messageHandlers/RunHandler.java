package messageHandlers;

import legacyfeed.SimpleMessage;
import model.InitialSubmission;
import model.Problem;
import model.Team;
import org.jfree.util.Log;

import java.security.InvalidKeyException;

public class RunHandler extends SingleMessageHandler {

	public RunHandler() {
		super("run");
	}
	
	private void processFreshSubmission(SimpleMessage message) {
		double secondsFromStart = message.getDouble("time");
		
		int minutesFromStart = (int) (secondsFromStart / 60.0);
		String submissionId = message.get("id");
		String problemId = message.get("problem");
		String teamId = message.get("team");
        String language = message.get("language");
		
		Team team;
		Problem problem;
		try {
			team = contest.getTeam(teamId);
			problem = contest.getProblem(problemId);
		} catch (InvalidKeyException e) {
			error(String.format("Unable to processLegacyFeed message %s. Reason: %s", message, e.getMessage()));
			return;
		}
		
		InitialSubmission newSubmission = new InitialSubmission(submissionId, team, problem, language,minutesFromStart * 60000);
		
		team.freshSubmission(newSubmission);
	}
	
	private void processFinalizedSubmission(SimpleMessage message) {
		double secondsFromStart = message.getDouble("time");
		int millisFromStart = (int) (secondsFromStart * 1000);
		
		String submissionId = message.get("id");
		String problemId = message.get("problem");
		String judgement = message.get("result");
		boolean solved = message.getBool("solved");
		boolean penalty = message.getBool("penalty");
		String teamId = message.get("team");
        String language = message.get("language");
		Boolean isJudged = message.getBool("judged");
		
		Team team;
		Problem problem;
		try {
			team = contest.getTeam(teamId);
			problem = contest.getProblem(problemId);
		} catch (InvalidKeyException e) {
			error(String.format("Unable to processLegacyFeed message %s. Reason: %s", message, e.getMessage()));
			return;
		}
		
		if (isJudged) {
			InitialSubmission submission = contest.getAnalyzer().submissionById(submissionId);
			team.submit(submission, millisFromStart, submissionId, problem, judgement, solved, penalty);
		} else {
			Log.info(String.format("%s judgement of %s has been judged. Outcome is not disclosed",
					team, problem));
		}	
		
	}
	
	@Override
	public void process(SimpleMessage message) {

		// for now, only consider solved submissions
		String status = message.get("status");
		String judged = message.get("judged");
		if ("fresh".equals(status) || "False".equals(judged)) {
			processFreshSubmission(message);
		}
		
		if ("done".equals(status) || "True".equals(judged)) {
			processFinalizedSubmission(message);
		}
		
	}

}
