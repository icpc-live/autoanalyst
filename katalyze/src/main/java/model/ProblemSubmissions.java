package model;

import java.util.*;

public class ProblemSubmissions implements Iterable<Submission>{

	private final Problem problem;

	private TreeSet<Submission> submissions = new TreeSet<>(Submission.compareBySubmissionTime);

	ProblemSubmissions(Problem problem) {
		this.problem = problem;
	}
	
	public boolean isSolved() {
		for(Submission s : submissions) {
			if (s.isAccepted()) {
				return true;
			}
		}
		return false;
	}
	

	public Submission[] toArray() {
		return submissions.toArray(new Submission[0]);
	}
	
	public int getSubmissionCount() {
		int count = 0;
		for (Submission s : submissions) {
			count++;
			if (s.isAccepted()) {
				break;
			}
		}
		return count;
	}
	
	public int getSolutionTime() {
		for (Submission s : submissions) {
			if (s.isAccepted()) {
				return s.initialSubmission.minutesFromStart;
			}
		}
		return 0;
	}
	
	public int penalty() {
		int cumulativeScore = 0;
		
		for (Submission s : submissions) {
				cumulativeScore += s.cost();
				if (s.isAccepted()) {
					return cumulativeScore;
				}
		}
		
		return cumulativeScore;
	}
	
	public int scoreContribution() {
		
		int cumulativeScore = 0;
		
		for (Submission s : submissions) {
				cumulativeScore += s.cost();
				if (s.isAccepted()) {
					return cumulativeScore;
				}
		}
		
		// No cost if no solution was accepted
		return 0;
	}

	public void add(Submission submission) {
		assert submission.getProblem() == problem;
		submissions.add(submission);
	}


    @Override
    public Iterator<Submission> iterator() {
        return submissions.iterator();
    }
}
