package model;

public interface ScoreTableEntry {

	public abstract Team getTeam();

	public abstract int getTimeIncludingPenalty();

	public abstract int getNumberOfSolvedProblems();

	public abstract int getLastAcceptedSubmission();
	
	public abstract boolean isSolved(Problem problem);	

}