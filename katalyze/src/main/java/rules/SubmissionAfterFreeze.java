package rules;

import model.*;

public class SubmissionAfterFreeze extends StateComparingRuleBase implements SolutionSubmittedEvent {

	// Only submissions of teams with rank <= threshold will result in a notification.
	final private int rankThreshold;

	public SubmissionAfterFreeze(int rankThreshold) {
		this.rankThreshold = rankThreshold;
	}

	public void onSolutionSubmitted(StandingsAtSubmission standingsAtSubmission) {
		Standings standingsBefore = standingsAtSubmission.before;
		Contest contest = standingsBefore.getContest();
		InitialSubmission submission = standingsAtSubmission.submission;

		if (!contest.isFrozen(submission.contestTimeMilliseconds)) {
			return;
		}

		Team team = submission.team;
		Score teamScore = standingsBefore.scoreOf(team);

		if (standingsBefore.rankOf(team) > rankThreshold) {
			return;
		}

		if (teamScore.isSolved(submission.getProblem())) {
			// Skip notifications for problems already solved.
			return;
		}

		String message = "{team} submitted a solution for {problem}.";
		LoggableEvent event = new LoggableEvent(contest, submission.contestTimeMilliseconds, message, EventImportance.Whatever, standingsAtSubmission.submission, null);
		notify(event);
	}

	public String toString() {
		return String.format("Submissions after freeze (rank <= %d)", rankThreshold);
	}
}

