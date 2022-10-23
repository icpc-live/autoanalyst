package model;

import java.util.Map;
import java.util.regex.Matcher;

public class LoggableEvent {
	private static int nextEventId = 0;
	
	
	public final Contest contest;
	public final int id;
	public final Team team;
	public final Problem problem;
	public final long contestTimeMillis;
	public final String message;
	public final String icatMessage;
	public final EventImportance importance;
	public final InitialSubmission submission;
	public final Map<String, String> supplements;
	
	public LoggableEvent(Contest contest, long contestTimeMillis, String message, EventImportance importance, InitialSubmission submission, Map<String,String> supplements) {
		this.id = nextEventId++;
		this.contest = contest;
		this.team = (submission != null) ? submission.getTeam() : null;
		this.contestTimeMillis = contestTimeMillis;
		this.importance = importance;
		this.submission = submission;
		if (this.submission != null) {
			this.problem = this.submission.problem;
		} else {
			this.problem = null;
		}
		this.supplements = supplements;

		this.message = getCleartextMessage(message);
		this.icatMessage = getICatMessage(message);

	}
	
	public LoggableEvent(Contest contest, Team team, int contestTimeMillis, String message, EventImportance importance, Map<String,String> supplements) {
		this.id = nextEventId++;
		this.contest = contest;
		this.team = team;
		this.contestTimeMillis = contestTimeMillis;
		this.message = message;
		this.icatMessage = message;
		this.importance = importance;
		this.supplements = supplements;
		this.submission = null;
		this.problem = null;
	}

	public int contestTimeMinutes() {
		return (int) (contestTimeMillis / 60000);
	}

	private static String replaceMarkup(String source, String tag, String replacement) {
		return source.replaceAll("\\{"+tag+"\\}", Matcher.quoteReplacement(replacement));
	}
	
	private String getCleartextMessage(String message) {
		if (problem != null) {
			message = replaceMarkup(message, "problem", problem.stringForCommentary());
		}
		if (team != null) {
			message = replaceMarkup(message, "team", team.stringForCommentary());
		}
		return message;
	}
	
	private String getICatMessage(String message) {
		if (problem != null) {
			message = replaceMarkup(message, "problem", problem.toString());
		}
		if (team != null) {
			message = replaceMarkup(message, "team", team.toString());
		}
		return message;
	}

}
