package model;

import java.util.Comparator;

public class Submission {
	// FIXME: this should be read from contest configuration.
	public static final int CostOfFailedSubmission = 20;
	
	final InitialSubmission initialSubmission;
	final String judgementId;
	final Team team;
	final int judgementTimeMillis;
	final boolean accepted;
	final boolean penalty;
	final String outcome;
	final Problem problem;
	final TestCaseExecution failingCase;

	public Submission(InitialSubmission initialSubmission, int judgementTimeMillis, String judgementId, Team team, Problem problem, String outcome, boolean accepted, boolean penalty, TestCaseExecution failingCase) {
		this.initialSubmission = initialSubmission;
		this.judgementId = judgementId;
		this.team = team;
		this.judgementTimeMillis = judgementTimeMillis;
		this.accepted = accepted;
		this.penalty = penalty;
		this.outcome = outcome;
		this.problem = problem;
        this.failingCase = failingCase;
	}

	public static final Comparator<Submission> compareBySubmissionTime = (o1, o2) -> Integer.compare(
			o1.initialSubmission.contestTimeMilliseconds,
			o2.initialSubmission.contestTimeMilliseconds);
	
	public boolean isAccepted() {
		return accepted;
	}
	
	public Problem getProblem() {
		return problem;
	}
	
	public String getOutcome() {
		if (!accepted && !penalty) {
			return "("+outcome+")";
		} else {
			return outcome;
		}
	}
	
	public String getDetailedOutcome() {
		String status = (failingCase == null)
				? outcome
				: String.format("%s on case %d", outcome, failingCase.caseNumber);
		if (!accepted && !penalty) {
			return "("+status+")";
		} else {
			return status;
		}		
	}
	
	public Team getTeam() {
		return team;
	}


	public int getJudgementTimeMillis() {
		return judgementTimeMillis;
	}
	

	public InitialSubmission getInitialSubmission() {
		return initialSubmission;
	}
	
	public int cost() {
		if (accepted) {
			return initialSubmission.minutesFromStart;
		} else {
			if (penalty) {
				return CostOfFailedSubmission;
			} else {
				return 0;
			}
		}
	}
	

}
