package model;

public class InitialSubmission {
	public final String id;
	public final Team team;
	public final Problem problem;
	public final int minutesFromStart;
	public final String language;
	
	public InitialSubmission(String id, int minutesFromStart, Team team, Problem problem, String language) {
		this.id = id;
		this.minutesFromStart = minutesFromStart;
		this.team = team;
		this.problem = problem;
		this.language = language;
	}
	
	public Team getTeam() {
		return team;
	}
	
	public Problem getProblem() {
		return problem;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public String getId() {
		return id;
	}
	
}
