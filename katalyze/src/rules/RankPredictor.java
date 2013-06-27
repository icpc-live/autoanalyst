package rules;

import java.util.ArrayList;

import model.*;

public class RankPredictor extends StateComparingRuleBase implements SolutionSubmittedEvent{
	final static ScoreTableComparer comparator = new ScoreTableComparer();
	
	final int rankThreshold;

	public RankPredictor(NotificationTarget target, int rankThreshold) {
		super(target);
		this.rankThreshold = rankThreshold;
	}
	
	public void onSolutionSubmitted(StandingsAtSubmission standingsAtSubmission) {
		Contest contest = standingsAtSubmission.before.getContest();
		Standings standingsBefore = standingsAtSubmission.before;
		InitialSubmission submission = standingsAtSubmission.submission;
		
		Team team = submission.team;
		
		Score teamScore = standingsBefore.scoreOf(team);		
		ScoreTableEntry fakeScore = FakeScore.PretendProblemSolved(teamScore, submission.problem, submission.minutesFromStart);
		
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
		int potentialRank = fakeIndex+2;
		
		if (potentialRank <= rankThreshold) {
			String message = String.format("{team} submitted solution for {problem}. If correct, they will get rank %d (%d)",
					potentialRank, currentRank);
			LoggableEvent event = new LoggableEvent(contest, message, EventImportance.Normal, standingsAtSubmission.submission);
			notify(event);						
		}
		
	}


}
