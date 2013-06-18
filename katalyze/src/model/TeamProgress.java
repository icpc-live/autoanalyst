package model;

import java.util.*;

public class TeamProgress {
	private final Map<Problem,ProblemSubmissions> submissions = new HashMap<Problem, ProblemSubmissions>();
	private final Team team;
	
	public TeamProgress(Team team) {
		this.team = team;
	}
	
	public Score calculateScore(int submissionsToConsider) {
		HashSet<Problem> solvedProblems = new HashSet<Problem>();
		
		int timeIncludingPenalty = 0;
		
		for (Problem p : submissions.keySet()) {
			ProblemSubmissions submissionsForProblem = submissions.get(p);
			if (submissionsForProblem.isSolvedByFirstSubmissions(submissionsToConsider)) {
				solvedProblems.add(p);				
				timeIncludingPenalty += submissionsForProblem.scoreContribution(submissionsToConsider);
			}
		}
		
		return new Score(team, timeIncludingPenalty, solvedProblems, submissions, submissionsToConsider);
	}
	
	private ProblemSubmissions getSubmissionsFor(Problem problem) {
		
		if (submissions.containsKey(problem)) {
			return submissions.get(problem);
		} else {
			ProblemSubmissions submissionsForProblem = new ProblemSubmissions(problem);
			submissions.put(problem, submissionsForProblem);
			return submissionsForProblem;
		}
	}

	public void register(Submission newSubmission) {
		ProblemSubmissions submissions = getSubmissionsFor(newSubmission.getProblem());
		submissions.add(newSubmission);
	}
	
	

}
