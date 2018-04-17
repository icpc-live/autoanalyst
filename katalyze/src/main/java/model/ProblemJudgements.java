package model;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;

public class ProblemJudgements implements Iterable<Judgement>{
    private static Logger log = LogManager.getLogger(ProblemJudgements.class);

	private final Problem problem;

	private TreeSet<Judgement> submissions = new TreeSet<>(Judgement.compareBySubmissionTime);

	ProblemJudgements(Problem problem) {
		this.problem = problem;
	}
	
	public boolean isSolved() {
		for(Judgement s : submissions) {
			if (s.isAccepted()) {
				return true;
			}
		}
		return false;
	}
	

	public Judgement[] toArray() {
		return submissions.toArray(new Judgement[0]);
	}
	
	public int getSubmissionCount() {
		int count = 0;
		for (Judgement s : submissions) {
			count++;
			if (s.isAccepted()) {
				break;
			}
		}
		return count;
	}
	
	public int getSolutionTime() {
		for (Judgement s : submissions) {
			if (s.isAccepted()) {
				return s.initialSubmission.minutesFromStart;
			}
		}
		return 0;
	}
	
	public int penalty() {
		int cumulativeScore = 0;
		
		for (Judgement s : submissions) {
				cumulativeScore += s.cost();
				if (s.isAccepted()) {
					return cumulativeScore;
				}
		}
		
		return cumulativeScore;
	}
	
	public int scoreContribution() {
		
		int cumulativeScore = 0;
		
		for (Judgement s : submissions) {
				cumulativeScore += s.cost();
				if (s.isAccepted()) {
					return cumulativeScore;
				}
		}
		
		// No cost if no solution was accepted
		return 0;
	}

	public void add(Judgement newSubmission) {
		assert newSubmission.getProblem() == problem;
		String submissionId = newSubmission.initialSubmission.getId();

		int countBefore = submissions.size();
		for (Judgement existingSubmission : submissions) {
		    InitialSubmission existing = existingSubmission.initialSubmission;

		    if (existing != null && submissionId.equals(existing.getId())) {
                log.info(String.format("Apparently a new judgment '%s' for submission id %s. %s -> %s", newSubmission.judgementId,
                        submissionId, existingSubmission.outcome, newSubmission.outcome));
                submissions.remove(existingSubmission);
                break;
            }

        }

        submissions.add(newSubmission);
	}


    @Override
    public Iterator<Judgement> iterator() {
        return submissions.iterator();
    }
}
