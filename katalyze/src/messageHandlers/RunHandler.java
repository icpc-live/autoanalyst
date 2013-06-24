package messageHandlers;

import java.security.InvalidKeyException;

import model.InitialSubmission;
import model.Problem;
import model.Team;
import io.SimpleMessage;

public class RunHandler extends SingleMessageHandler {

	public RunHandler() {
		super("run");
	}
	
	private void processFreshSubmission(SimpleMessage message) {
		double secondsFromStart = message.getDouble("time");
		
		int minutesFromStart = (int) (secondsFromStart / 60.0);
		int submissionId = message.getInt("id");
		String problemId = message.get("problem");
		int teamNumber = message.getInt("team");
        String language = message.get("language");
		
		Team team;
		Problem problem;
		try {
			team = contest.getTeam(teamNumber);
			problem = contest.getProblem(problemId);
		} catch (InvalidKeyException e) {
			error(String.format("Unable to process message %s. Reason: %s", message, e.getMessage()));
			return;
		}
		
		InitialSubmission newSubmission = new InitialSubmission(submissionId, minutesFromStart, team, problem, language);
		
		team.freshSubmission(newSubmission);
	}
	
	private void processJudgedSubmission(SimpleMessage message) {
		double secondsFromStart = message.getDouble("time");
		
		int minutesFromStart = (int) (secondsFromStart / 60.0);
		int submissionId = message.getInt("id");
		String problemId = message.get("problem");
		String judgement = message.get("result");
		boolean solved = message.getBool("solved");
		boolean penalty = message.getBool("penalty");
		int teamNumber = message.getInt("team");
        String language = message.get("language");
		
		Team team;
		Problem problem;
		try {
			team = contest.getTeam(teamNumber);
			problem = contest.getProblem(problemId);
		} catch (InvalidKeyException e) {
			error(String.format("Unable to process message %s. Reason: %s", message, e.getMessage()));
			return;
		}
		
		team.submit(submissionId, problem, minutesFromStart, judgement, solved, penalty, language);
	}
	
	@Override
	public void process(SimpleMessage message) {

		// for now, only consider solved submissions
		String status = message.get("status");
		if ("fresh".equals(status)) {
			processFreshSubmission(message);
		}
		
		if ("done".equals(status)) {
			processJudgedSubmission(message);
		}
		
	}

}
