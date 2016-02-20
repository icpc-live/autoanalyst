package model;

public class Submission {
	// FIXME: this should be read from contest configuration.
	public static final int CostOfFailedSubmission = 20;
	
	final InitialSubmission initialSubmission;
	final int serialNumber;
	final Team team;
	final int minutesFromStart;
	final boolean accepted;
	final boolean penalty;
	final String outcome;
	final Problem problem;
	final String language;
	final TestCaseExecution failingCase;

	public Submission(InitialSubmission initialSubmission, int serialNumber, Team team, int minutesFromStart, Problem problem, String outcome, boolean accepted, boolean penalty, String language, TestCaseExecution failingCase) {
		this.initialSubmission = initialSubmission;
		this.serialNumber = serialNumber;
		this.team = team;
		this.minutesFromStart = minutesFromStart;
		this.accepted = accepted;
		this.penalty = penalty;
		this.outcome = outcome;
		this.problem = problem;
        this.language = language;
        this.failingCase = failingCase;
	}
	
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

	public String getLanguage() {
		return language;
	}
	
	public int getMinutesFromStart() {
		return minutesFromStart;
	}
	
	public int getSerialNumber() {
		return serialNumber;
	}
	
	public InitialSubmission getInitialSubmission() {
		return initialSubmission;
	}
	
	public int cost() {
		if (accepted) {
			return minutesFromStart;
		} else {
			if (penalty) {
				return CostOfFailedSubmission;
			} else {
				return 0;
			}
		}
	}
	
	public boolean isNewerThan(Submission other) {
		return (minutesFromStart > other.minutesFromStart);
	}
	
}
