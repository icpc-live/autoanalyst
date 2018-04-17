package model;

import java.util.*;

public class Score implements ScoreTableEntry {
	final Team team;
	final int points;
	final Set<Problem> solvedProblems;
	final Map<Problem, ProblemJudgements> submissions;
	int lastAcceptedSubmission = Integer.MIN_VALUE;

	public Score(Team team, int points, Set<Problem> solvedProblems, Map<Problem, ProblemJudgements> submissions) {
		this.team = team;
		this.points = points;
		this.solvedProblems = new HashSet<Problem>(solvedProblems);
		this.submissions = submissions;
	}
	
	public int getNumberOfSolvedProblems() {
		return solvedProblems.size();
	}
	
	public int getLastAcceptedSubmission() {
		if (lastAcceptedSubmission == Integer.MIN_VALUE) {
			int lastTime = 0;
			for (Problem p : solvedProblems) {
				ProblemJudgements subsForProblem = submissions.get(p);
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
		ProblemJudgements problemJudgements = submissions.get(problem);
		return problemJudgements.getSubmissionCount();
	}
	
	public int solutionTime(Problem problem) {
		if (!submissions.containsKey(problem)) {
			return 0;
		}
		ProblemJudgements problemJudgements = submissions.get(problem);
		return problemJudgements.getSolutionTime();
	}
	
	public int scoreContribution(Problem problem) {
		if (!submissions.containsKey(problem)) {
			return 0;
		}
		ProblemJudgements problemJudgements = submissions.get(problem);
		return problemJudgements.scoreContribution();
	}
	
	public int penaltyIfSolved(Problem problem) {
		if (!submissions.containsKey(problem)) {
			return 0;
		}
		ProblemJudgements problemJudgements = submissions.get(problem);
		return problemJudgements.penalty();
	}
	
	public Judgement[] submissionsFor(Problem problem) {
		if (!submissions.containsKey(problem)) {
			return new Judgement[0];
		}
		
		ProblemJudgements problemJudgements = submissions.get(problem);
		return problemJudgements.toArray();
	}

    public int lastSubmissionTime(Problem problem) {
        int lastRelevantTime = 0;

        ProblemJudgements problemJudgements = submissions.get(problem);
        if (problemJudgements != null) {

            for (Judgement s : problemJudgements) {
            	InitialSubmission initialSubmission = s.getInitialSubmission();
                if (s.isAccepted()) {
                    return initialSubmission.minutesFromStart;
                }
                lastRelevantTime = initialSubmission.minutesFromStart;
            }
        }

        return lastRelevantTime;

    }

	
	

}
