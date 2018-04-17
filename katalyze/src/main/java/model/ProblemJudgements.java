package model;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;

public class ProblemJudgements implements Iterable<Judgement>{
    private static Logger log = LogManager.getLogger(ProblemJudgements.class);
    private static TeamNameAsOrganization teamNameMapper = new TeamNameAsOrganization();

	private final Problem problem;

	private TreeSet<Judgement> judgements = new TreeSet<>(Judgement.compareBySubmissionTime);

	ProblemJudgements(Problem problem) {
		this.problem = problem;
	}
	
	public boolean isSolved() {
		for(Judgement s : judgements) {
			if (s.isAccepted()) {
				return true;
			}
		}
		return false;
	}
	

	public Judgement[] toArray() {
		return judgements.toArray(new Judgement[0]);
	}
	
	public int getSubmissionCount() {
		int count = 0;
		for (Judgement s : judgements) {
			count++;
			if (s.isAccepted()) {
				break;
			}
		}
		return count;
	}
	
	public int getSolutionTime() {
		for (Judgement s : judgements) {
			if (s.isAccepted()) {
				return s.initialSubmission.minutesFromStart;
			}
		}
		return 0;
	}
	
	public int penalty() {
		int cumulativeScore = 0;
		
		for (Judgement s : judgements) {
				cumulativeScore += s.cost();
				if (s.isAccepted()) {
					return cumulativeScore;
				}
		}
		
		return cumulativeScore;
	}
	
	public int scoreContribution() {
		
		int cumulativeScore = 0;
		
		for (Judgement s : judgements) {
				cumulativeScore += s.cost();
				if (s.isAccepted()) {
					return cumulativeScore;
				}
		}
		
		// No cost if no solution was accepted
		return 0;
	}

	private Judgement findExistingJudgement(String submissionId) {
		for (Judgement existingJudgement: judgements) {
			InitialSubmission existing = existingJudgement.initialSubmission;
			if (existing.getId().equals(submissionId)) {
				return existingJudgement;
			}
		}
		return null;
	}


	public void add(Judgement newJudgement) {
		assert newJudgement.getProblem() == problem;
		String submissionId = newJudgement.initialSubmission.getId();

		Judgement existingJudgement = findExistingJudgement(submissionId);

		if (existingJudgement != null) {
            boolean outcomesAreIdentical = Objects.equals(existingJudgement.getOutcome(), newJudgement.getOutcome());

            if (!outcomesAreIdentical) {
                if (Objects.equals(existingJudgement.getJudgementId(), newJudgement.getJudgementId())) {
                    log.warn("Whoa!! Once set, a judgement should not change its outcome!");
                }

                judgements.removeIf(x -> submissionId.equals(x.getInitialSubmission().getId()));
                boolean resolvedRegardless = isSolved();

                String redundant = (resolvedRegardless) ? "redundant " : "";

                log.info(String.format("New %sjudgement '%s' for %s changed outcome for judgement id %s. %s -> %s", redundant, newJudgement.judgementId,
                        teamNameMapper.apply(newJudgement.initialSubmission.getTeam()), submissionId, existingJudgement.outcome, newJudgement.outcome));

            }


        }
        judgements.add(newJudgement);
	}


    @Override
    public Iterator<Judgement> iterator() {
        return judgements.iterator();
    }
}
