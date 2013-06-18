package model;

public class Submission {
	public static final int CostOfFailedSubmission = 20;
	
	final int serialNumber;
	final Team team;
	final int minutesFromStart;
	final boolean accepted;
	final boolean penalty;
	final String outcome;
	final Problem problem;
	final String language;

	public Submission(int serialNumber, Team team, int minutesFromStart, Problem problem, String outcome, boolean accepted, boolean penalty, String language) {
		this.serialNumber = serialNumber;
		this.team = team;
		this.minutesFromStart = minutesFromStart;
		this.accepted = accepted;
		this.penalty = penalty;
		this.outcome = outcome;
		this.problem = problem;
        this.language = language;
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
