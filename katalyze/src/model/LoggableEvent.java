package model;

public class LoggableEvent {
	private static int nextEventId = 0;
	
	
	public final Contest contest;
	public final int id;
	public final Team team;
	public final int time;
	public final String message;
	public final String icatMessage;
	public final EventImportance importance;
	public final InitialSubmission submission;
	
	public LoggableEvent(Contest contest, String message, EventImportance importance, InitialSubmission submission) {
		this.id = nextEventId++;
		this.contest = contest;
		this.team = submission.getTeam();
		this.time = submission.minutesFromStart;
		this.message = getCleartextMessage(message, submission);
		this.icatMessage = getICatMessage(message, submission);
		this.importance = importance;
		this.submission = submission;
	}
	
	private static String getCleartextMessage(String message, InitialSubmission submission) {
		if (submission != null) {
			message = message.replaceAll("\\{problem\\}", submission.getProblem().getName());
			message = message.replaceAll("\\{team\\}", submission.getTeam().getName());
		}
		return message;
	}
	
	private static String getICatMessage(String message, InitialSubmission submission) {
		if (submission != null) {
			message = message.replaceAll("\\{problem\\}", submission.getProblem().toString());
			message = message.replaceAll("\\{team\\}", submission.getTeam().toString());
		}
		return message;
	}

}
