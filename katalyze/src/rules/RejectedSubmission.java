package rules;

import model.*;

public class RejectedSubmission extends StateComparingRuleBase implements StandingsUpdatedEvent{

	final int numberOfPositionsToMonitor;
	
	public RejectedSubmission(int positionsToMonitor) {
		this.numberOfPositionsToMonitor = positionsToMonitor;
	}

	
	@Override
	public void onStandingsUpdated(StandingsTransition transition) {
		// This rule doesn't bother about accepted solutions
		Submission submission = transition.submission;
		
		if (submission.isAccepted()) {
			return;
		}
		
		int rankAfter = transition.after.rankOf(submission.getTeam());
		EventImportance defaultImportance = (rankAfter < numberOfPositionsToMonitor) ? EventImportance.Normal : EventImportance.Whatever;
		
		Score teamScore = transition.before.scoreOf(submission.getTeam());
		Submission[] previousSubmissions = teamScore.submissionsFor(submission.getProblem());

		
		
		if (previousSubmissions.length == 1) {
		
			notify(transition.createEvent(String.format("{team} fails its first attempt on {problem} due to %s", submission.getDetailedOutcome()), defaultImportance));
		} else {
			String previousAttempts = "";
			boolean first = true;
			for (Submission s : previousSubmissions) {
				if (first) {
					first = false;
				} else {
					previousAttempts += " " + s.getOutcome();
				}
			}
			
			notify(transition.createEvent(String.format("{team} fails again on {problem} due to %s. Previous attempts:%s", 
					submission.getDetailedOutcome(), previousAttempts), defaultImportance));
		}
		
	}

}
