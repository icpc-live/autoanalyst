package model;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;

public class ProblemSubmissions implements Iterable<Submission>{
    private static Logger log = LogManager.getLogger(ProblemSubmissions.class);

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

	public void add(Submission newSubmission) {
		assert newSubmission.getProblem() == problem;
		String submissionId = newSubmission.initialSubmission.getId();

		int countBefore = submissions.size();
		submissions.removeIf(x -> x.initialSubmission.id.equals(submissionId));

		int countAfter = submissions.size();
		if (countAfter != countBefore) {
		    log.info(String.format("Apparently a new judgment '%s' for submission id %s", newSubmission.judgementId,
                    submissionId));
        }
		submissions.add(newSubmission);
	}


    @Override
    public Iterator<Submission> iterator() {
        return submissions.iterator();
    }
}
