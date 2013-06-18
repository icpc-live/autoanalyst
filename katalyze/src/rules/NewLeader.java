package rules;

import model.EventImportance;
import model.LoggableEvent;
import model.NotificationTarget;
import model.Score;
import model.Submission;
import model.Team;

public class NewLeader extends StateComparingRuleBase {

	private int numberOfPositionsToMonitor;
	
	public NewLeader(NotificationTarget target, int numberOfPositionsToMonitor) {
		super(target);
		this.numberOfPositionsToMonitor = numberOfPositionsToMonitor;
	}
	
	private EventImportance fromRank(int rank) {
		if (rank <= 4) {
			return EventImportance.Breaking;
		}
		if (rank < numberOfPositionsToMonitor) {
			return EventImportance.Normal;
		}
		return EventImportance.Whatever;
	}
	
	private String problemsAsText(int nProblems) {
		if (nProblems == 1) {
			return "1 problem";
		} else {
			return String.format("%d problems", nProblems);
		}
	}
	
	@Override
	public void onStandingsUpdated(StandingsTransition transition) {
		
		Submission submission = transition.submission;
		
		if (!submission.isAccepted()) {
			return;
		}
		
		Team team = submission.getTeam();

		Score scoreBefore = transition.before.scoreOf(team);
		if (scoreBefore.isSolved(submission.getProblem())) {
			// Problem was already solved. No need to send new notifications
			return;
		}
				
		
		int rankBefore = transition.before.rankOf(team);
		int rankAfter = transition.after.rankOf(team);
		Score score = transition.after.scoreOf(team);
		
		int solvedProblemCount = score.solvedProblemCount();
		EventImportance defaultImportance = fromRank(rankAfter);

		String solvedProblemsText = problemsAsText(solvedProblemCount);
		
		LoggableEvent event = null;
		
		
		if (solvedProblemCount == 1) {
			event = transition.createEvent(String.format("{team} solves its first problem: {problem}"), EventImportance.Normal);
		} else if (rankAfter == 1 && rankBefore == 1) {
			event = transition.createEvent(String.format("{team} extends its lead by solving {problem}. It has now solved %s", solvedProblemsText),  EventImportance.Breaking);
		} else if (rankAfter < rankBefore) {
			if (rankAfter == 1) {
				event = transition.createEvent(String.format("{team} now leads the competition after solving {problem}. It has now solved %s", solvedProblemsText),  EventImportance.Breaking);
			} else {
				event = transition.createEvent(String.format("{team} solves {problem}. It has now solved %s and is at rank %d (%d)", solvedProblemsText, rankAfter, rankBefore), defaultImportance);
			}
		} else if (rankAfter == rankBefore) {
			event = transition.createEvent(String.format("{team} solves {problem}. It has now solved %s, but is still at rank %d", solvedProblemsText, rankAfter), defaultImportance);
		} else {
			assert rankAfter <= rankBefore;
		}
		
		target.notify(event);
	}

}
