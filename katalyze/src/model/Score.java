package model;

import java.util.*;

public class Score implements ScoreTableEntry {
	final Team team;
	final int points;
	final Set<Problem> solvedProblems;
	final Map<Problem,ProblemSubmissions> submissions;
	final int consideredSubmissionCount;
	int lastAcceptedSubmission = Integer.MIN_VALUE;

	public Score(Team team, int points, Set<Problem> solvedProblems, Map<Problem,ProblemSubmissions> submissions, int consideredSubmissionCount) {
		this.team = team;
		this.points = points;
		this.solvedProblems = new HashSet<Problem>(solvedProblems);
		this.submissions = submissions;
		this.consideredSubmissionCount = consideredSubmissionCount;
	}
	
	public int getNumberOfSolvedProblems() {
		return solvedProblems.size();
	}
	
	public int getLastAcceptedSubmission() {
		if (lastAcceptedSubmission == Integer.MIN_VALUE) {
			int lastTime = 0;
			for (Problem p : solvedProblems) {
				ProblemSubmissions subsForProblem = submissions.get(p);
				int solutionTime = subsForProblem.getSolutionTime();
				if (solutionTime > lastTime) {
					lastTime = solutionTime;
				}
			}
			lastAcceptedSubmission = lastTime;
		}
		return lastAcceptedSubmission;		
	}

	
	/* (non-Javadoc)
	 * @see model.ScoreTableEntry#getTeam()
	 */
	public Team getTeam() {
		return team;
	}
	
	
	/* (non-Javadoc)
	 * @see model.ScoreTableEntry#getTimeIncludingPenalty()
	 */
	public int getTimeIncludingPenalty() {
		return points;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Score) {
			Score other = (Score) obj;
			return 
				(other.points == this.points) &&
				(other.solvedProblems.equals(this.solvedProblems)) &&
				(other.getLastAcceptedSubmission() == getLastAcceptedSubmission());
		} else {
			return false;
		}
	}
	

	@Override
	public String toString() {
		return String.format("Team %s, %d/%d", team.toString(), points, getNumberOfSolvedProblems());
	}

	@Override
	public int hashCode() {
		return points;
	}

	public int solvedProblemCount() {
		return solvedProblems.size();
	}

	public boolean isSolved(Problem problem) {
		return solvedProblems.contains(problem);
	}
	
	public int submissionCount(Problem problem) {
		if (!submissions.containsKey(problem)) {
			return 0;
		}
		ProblemSubmissions problemSubmissions = submissions.get(problem);
		return problemSubmissions.getSubmissionCount();
	}
	
	public int solutionTime(Problem problem) {
		if (!submissions.containsKey(problem)) {
			return 0;
		}
		ProblemSubmissions problemSubmissions = submissions.get(problem);
		return problemSubmissions.getSolutionTime();
	}
	
	public int scoreContribution(Problem problem) {
		if (!submissions.containsKey(problem)) {
			return 0;
		}
		ProblemSubmissions problemSubmissions = submissions.get(problem);
		return problemSubmissions.scoreContribution(consideredSubmissionCount);
	}
	
	public int penaltyIfSolved(Problem problem) {
		if (!submissions.containsKey(problem)) {
			return 0;
		}
		ProblemSubmissions problemSubmissions = submissions.get(problem);
		return problemSubmissions.penalty(consideredSubmissionCount);
	}
	
	public Submission[] submissionsFor(Problem problem) {
		if (!submissions.containsKey(problem)) {
			return new Submission[0];
		}
		
		ProblemSubmissions problemSubmissions = submissions.get(problem);
		return problemSubmissions.toArray();
	}
	
	
	
	

}
