package model;

public class FakeScore implements ScoreTableEntry {

	int nSolvedProblems;
	Team team;
	int points;
	Problem additionalSolvedProblem;
	Score score;
	
	
	public static ScoreTableEntry PretendProblemSolved(Score score, Problem p, int contestTime) {
		// If already solved, this changes nothing. 
		
		if (score.isSolved(p)) {
			return score;
		}
		
		FakeScore target = new FakeScore();
		target.nSolvedProblems = score.getNumberOfSolvedProblems()+1;
		target.team = score.getTeam();
		target.points = score.getTimeIncludingPenalty() + score.penaltyIfSolved(p) + contestTime;
		target.additionalSolvedProblem = p;
		target.score = score;
		return target;
		
	}
	

	@Override
	public int getLastAcceptedSubmission() {
		// Always later than any present score
		return Integer.MAX_VALUE;
	}
	
	@Override
	public boolean isSolved(Problem p) {
		return (score.isSolved(p) || p.equals(additionalSolvedProblem));
	}

	@Override
	public int getNumberOfSolvedProblems() {
		// TODO Auto-generated method stub
		return nSolvedProblems;
	}

	@Override
	public Team getTeam() {
		return team; 
	}

	@Override
	public int getTimeIncludingPenalty() {
		return points;
	}

}
