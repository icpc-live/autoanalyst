package rules;

import model.*;

public class ProblemFirstSolved extends StateComparingRuleBase implements StandingsUpdatedEvent{
	
	public ProblemFirstSolved(NotificationTarget target) {
		super(target);
	}
	
	@Override
	public void onStandingsUpdated(StandingsTransition transition) {
		Submission submission = transition.submission;
		
		if (!submission.isAccepted()) {
			return;
		}
	
		Problem submittedProblem = submission.getProblem();
		if (!transition.before.isSolved(submittedProblem)) {
			notify(transition.createEvent( "{team} is the first team to solve problem {problem}", EventImportance.Breaking));
		}
	}
	

}
