package rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.*;

public class RankPredictor extends StateComparingRuleBase implements SolutionSubmittedEvent{
	final private static ScoreTableComparer comparator = new ScoreTableComparer();
	
	final private int rankThreshold;




	public RankPredictor(int rankThreshold) {
		this.rankThreshold = rankThreshold;
	}
	
	public void onSolutionSubmitted(StandingsAtSubmission standingsAtSubmission) {
		Contest contest = standingsAtSubmission.before.getContest();

		Standings standingsBefore = standingsAtSubmission.before;
		InitialSubmission submission = standingsAtSubmission.submission;
		if (contest.isFrozen(submission.contestTimeMilliseconds)) {
			// Don't make predictions when the scoreboard is frozen.
			return;
		}
		
		Team team = submission.team;
		ArrayList<InitialSubmission> submissionsForProblem = team.openSubmissions(submission.problem);
		if (submissionsForProblem.size()>1) {
		    logger.info(String.format("Skipping rank prediction for team %s as it has %d outstanding submissions on problem %s",
                    TeamNameAsOrganization.instance.apply(team), submissionsForProblem.size(), submission.getProblem().getLabel()));
		     return;
        }

		Score teamScore = standingsBefore.scoreOf(team);
		if (teamScore.isSolved(submission.getProblem())) {
			String message = "Despite already having solved it, {team} submitted a solution for {problem}";
			LoggableEvent event = new LoggableEvent(contest, submission.contestTimeMilliseconds, message, EventImportance.Whatever, standingsAtSubmission.submission, null);
			notify(event);
			return;
		}
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

			String message = String.format("{team} submitted solution for {problem}. If correct, they will %s",
					futureRankString(potentialRank, currentRank));
			Map<String, String> supplements = new HashMap<String, String>();
			supplements.put("currentRank", Integer.toString(currentRank));
			supplements.put("potentialRank", Integer.toString(potentialRank));
			LoggableEvent event = new LoggableEvent(contest, submission.contestTimeMilliseconds, message, EventImportance.Normal, submission, supplements);
			notify(event);
		}
		
	}

	public String toString() {
		return String.format("Rank Predictor (rank <= %d)", rankThreshold);
	}


}
