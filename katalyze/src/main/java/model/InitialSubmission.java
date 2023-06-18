package model;

import java.time.Instant;

public class InitialSubmission {
	public final String id;
	public final Team team;
	public final Problem problem;
	public final long contestTimeMilliseconds;
	public final int minutesFromStart;
	public final Instant timestamp;
	public final String language;
	
	public InitialSubmission(String id, Team team, Problem problem, String language, long contestTimeMilliseconds, Instant timestamp) {
		this.id = id;
		this.minutesFromStart = (int) (contestTimeMilliseconds/60000);
		this.contestTimeMilliseconds = contestTimeMilliseconds;
		this.team = team;
		this.problem = problem;
		this.language = language;
		this.timestamp = timestamp;
	}

	public InitialSubmission(String id, Team team, Problem problem, String language, long contestTimeMilliseconds) {
		this.id = id;
		this.minutesFromStart = (int) (contestTimeMilliseconds/60000);
		this.contestTimeMilliseconds = contestTimeMilliseconds;
		this.team = team;
		this.problem = problem;
		this.language = language;
		this.timestamp = team.getContest().getStartTime().plusMillis(contestTimeMilliseconds);
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
