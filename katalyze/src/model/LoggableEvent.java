package model;

import java.util.Map;
import java.util.regex.Matcher;

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
	public final Map<String, String> supplements;
	
	public LoggableEvent(Contest contest, String message, EventImportance importance, InitialSubmission submission, Map<String,String> supplements) {
		this.id = nextEventId++;
		this.contest = contest;
		this.team = submission.getTeam();
		this.time = submission.minutesFromStart;
		this.message = getCleartextMessage(message, submission);
		this.icatMessage = getICatMessage(message, submission);
		this.importance = importance;
		this.submission = submission;
		this.supplements = supplements;
	}
	
	public LoggableEvent(Contest contest, Team team, String message, int contestTime, EventImportance importance) {
		this.id = nextEventId++;
		this.contest = contest;
		this.team = team;
		this.time = contestTime;
		this.message = message;
		this.icatMessage = message;
		this.importance = importance;
		this.supplements = null;
		this.submission = null;
	}

	private static String replaceMarkup(String source, String tag, String replacement) {
		return source.replaceAll("\\{"+tag+"\\}", Matcher.quoteReplacement(replacement));
	}
	
	private static String getCleartextMessage(String message, InitialSubmission submission) {
		if (submission != null) {
			message = replaceMarkup(message, "problem", submission.getProblem().getName());
			message = replaceMarkup(message, "team", submission.getTeam().getName());
		}
		return message;
	}
	
	private static String getICatMessage(String message, InitialSubmission submission) {
		if (submission != null) {
			message = replaceMarkup(message, "problem", submission.getProblem().toString());
			message = replaceMarkup(message, "team", submission.getTeam().toString());
		}
		return message;
	}

}
