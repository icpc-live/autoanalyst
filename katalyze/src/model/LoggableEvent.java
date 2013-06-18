package model;

public class LoggableEvent {
	public final Contest contest;
	public final int id;
	public final Team team;
	public final int time;
	public final String message;
	public final String icatMessage;
	public final Score score;
	public final EventImportance importance;
	
	public LoggableEvent(int id, Contest contest, Team team, int time, String clearTextMessage, String icatMessage, Score score, EventImportance importance) {
		this.id = id;
		this.contest = contest;
		this.team = team;
		this.time = time;
		this.message = clearTextMessage;
		this.icatMessage = icatMessage;
		this.score = score;
		this.importance = importance;
	}
}
