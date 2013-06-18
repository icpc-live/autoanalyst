package model;

import rules.*;
import java.util.*;


import org.apache.log4j.Logger;

public class Analyzer implements NotificationTarget {
	
	final Contest contest;
	static Logger logger = Logger.getLogger(Analyzer.class);
	List<StandingsUpdatedEvent> stateRules = new ArrayList<StandingsUpdatedEvent>();
	List<NotificationTarget> targets = new ArrayList<NotificationTarget>();
	List<OutputHook> outputHooks = new ArrayList<OutputHook>();
	final static ScoreTableComparer comparator = new ScoreTableComparer();
	int lastHookTime = -1;
	int videoCaptureTreshold;
	int nextEventId = 0;
	
	public Analyzer(Contest contest, int videoCaptureTreshold) {
		this.contest = contest;
		this.videoCaptureTreshold = videoCaptureTreshold;
	}
	
	public void addRule(StandingsUpdatedEvent newRule) {
		logger.info(String.format("Activating rule %s", newRule));
		stateRules.add(newRule);
	}
	
	public void addNotifier(NotificationTarget newNotifier) {
		targets.add(newNotifier);
	}
	
	public void notify(LoggableEvent event) {
		for (NotificationTarget target : targets) {
			target.notify(event);
		}
	}
	
	private static String getCleartextMessage(String message, Submission submission) {
		if (submission != null) {
			message = message.replaceAll("\\{problem\\}", submission.getProblem().getName());
			message = message.replaceAll("\\{team\\}", submission.getTeam().getName());
		}
		return message;
	}
	
	private static String getICatMessage(String message, Submission submission) {
		if (submission != null) {
			message = message.replaceAll("\\{problem\\}", submission.getProblem().toString());
			message = message.replaceAll("\\{team\\}", submission.getTeam().toString());
		}
		return message;
	}
	
	
	public LoggableEvent createEvent(Submission submission, String message, EventImportance importance) {
		
		int minutesFromStart = submission.getMinutesFromStart();
		Team team = submission.getTeam();
		Score score = team.getScore(submission.getSerialNumber() + 1);

		String clearTextMessage = getCleartextMessage(message, submission);
		String icatMessage = getICatMessage(message, submission);
		
		LoggableEvent event = new LoggableEvent(nextEventId, contest, submission.getTeam(), minutesFromStart, clearTextMessage, icatMessage, score, importance);
		
		nextEventId++;
		return event;
	}
	
	
	
	
	private void processRules(Standings before, Standings after, Submission submission) {
		StandingsTransition transition = new StandingsTransition(this, before, after, submission);
		for (StandingsUpdatedEvent rule : stateRules) {
			rule.onStandingsUpdated(transition);
		}
	}

	public void notifySubmission(Submission newSubmission) {
		int submissionSerial = newSubmission.getSerialNumber();
		Standings before = contest.getStandings(submissionSerial);
		Standings after = contest.getStandings(submissionSerial+1);
		processRules(before, after, newSubmission);
		notifyHooks(newSubmission.minutesFromStart);
	}
	
	public void freshSubmission(Team team, Problem problem, int minutesFromStart) {
		
		Standings standingsBefore = contest.getStandings();
		
		Score teamScore = standingsBefore.scoreOf(team);
		
		ScoreTableEntry fakeScore = FakeScore.PretendProblemSolved(teamScore, problem, minutesFromStart);
		
		ArrayList<ScoreTableEntry> scoresAbove = new ArrayList<ScoreTableEntry>();

		int currentRank = standingsBefore.rankOf(team);
		
		for (ScoreTableEntry candidate : standingsBefore) {
			if (candidate != teamScore) {
				scoresAbove.add(candidate);
			} else {
				break;
			}
		}
		
		int fakeIndex = scoresAbove.size()-1;
		
		while (fakeIndex>=0 && comparator.compare(fakeScore, scoresAbove.get(fakeIndex))<=0) {
			fakeIndex--;
		}
		int rank = fakeIndex+2;
		
		if (rank <= 6) {
			String message = String.format("Team %s submitted solution for %s. If correct, they will get rank %d (%d)",
					team.getName(), problem.getName(), rank, currentRank);



			/* Disabled video capture for now
			if (minutesFromStart > videoCaptureTreshold) {
				String myCommand = String.format("/home/analyst10/record.sh %d", team.getTeamNumber());
			
				try {
					Runtime.getRuntime().exec(myCommand);
				}
				catch (Exception e) {
					logger.error(e.getMessage());
				}
			} */
			
			
			logger.info(message);
		}
				
		
	}
	

	private void notifyHooks(int minutesFromStart) {
		while (lastHookTime < minutesFromStart) {
			lastHookTime++;
			
			for(OutputHook hook : outputHooks) {
				hook.execute(lastHookTime);
			}
		}

	}

	public void addOutputHook(OutputHook outputHook) {
		outputHooks.add(outputHook);
	}
	
	public List<OutputHook> getOutputHooks() {
		return new ArrayList<OutputHook>(outputHooks);
	}
	

}
