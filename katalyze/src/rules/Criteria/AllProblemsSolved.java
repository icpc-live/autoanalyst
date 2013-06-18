package rules.Criteria;

import model.Contest;
import model.Problem;
import model.Standings;
import rules.StandingsCriterion;

public class AllProblemsSolved implements StandingsCriterion {

	@Override
	public boolean isFulfilled(Standings standings) {
		Contest contest = standings.getContest();
		for (Problem problem : contest.getProblems()) {
			if (!standings.isSolved(problem)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String message() {
		return "All problems have now been solved";
	}
	

}
