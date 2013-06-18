package rules.Criteria;

import rules.StandingsCriterion;
import model.Score;
import model.Standings;

public class AllTeamsSolvedOneProblem implements StandingsCriterion {
	
	public boolean isFulfilled(Standings standings) {
		for (Score score : standings) {
			if (score.solvedProblemCount() == 0) {
				return false;
			}
		}
		return true;		
	}
	
	public String message() {
		return "All teams have solved at least one problem";
	}

}
