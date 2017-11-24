package model;

public class TeamOutlook {
	
	Score bestScore;
	Score teamScore;
	Contest contest;
	
	public TeamOutlook(Contest contest, Score bestScore, Score teamScore) {
		this.contest = contest;
		this.bestScore = bestScore;
		this.teamScore = teamScore;
	}
	
	public int submissionRequiredWithinToLead(int contestTime) {
		// If the team can submit one more problem and get the first ranking, this returns within how
		// many minutes this must be done.
		
		int teamSolvedProblemCount = teamScore.solvedProblemCount();
		int timeLeft = contest.lengthInMinutes - contestTime;
		
		assert teamSolvedProblemCount <= bestScore.solvedProblemCount();
		
		if (teamSolvedProblemCount == bestScore.solvedProblemCount() &&
				teamSolvedProblemCount < contest.problems.size()) {
			return timeLeft;
		}
		
		if (teamSolvedProblemCount == bestScore.solvedProblemCount()-1) {
			int totalTeamTime = teamScore.getTimeIncludingPenalty();
			int bestTeamTime = bestScore.getTimeIncludingPenalty();
			
			int timeLimit = Math.min(bestTeamTime - totalTeamTime - contestTime - 1, timeLeft);
			
			if (timeLimit >= 0) {
				return timeLimit;
			}
		}
		
		return -1;
	}

}
