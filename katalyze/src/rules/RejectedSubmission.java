package rules;

import model.*;

public class RejectedSubmission extends StateComparingRuleBase {

	final int numberOfPositionsToMonitor;
	
	public RejectedSubmission(NotificationTarget notificationTarget, int positionsToMonitor) {
		super(notificationTarget);
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
		
			target.notify(transition.createEvent(String.format("{team} fails its first attempt on {problem} due to %s", submission.getOutcome()), defaultImportance));
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
			
			target.notify(transition.createEvent(String.format("{team} fails again on {problem} due to %s. Previous attempts:%s", 
					submission.getOutcome(), previousAttempts), defaultImportance));
		}
		
	}

}
