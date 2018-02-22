package model;

import java.util.Map;
import java.util.regex.Matcher;

public class LoggableEvent {
	private static int nextEventId = 0;
	
	
	public final Contest contest;
	public final int id;
	public final Team team;
	public final Problem problem;
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
	
	public LoggableEvent(Contest contest, Team team, String message, int contestTime, EventImportance importance, Map<String,String> supplements) {
		this.id = nextEventId++;
		this.contest = contest;
		this.team = team;
		this.time = contestTime;
		this.message = message;
		this.icatMessage = message;
		this.importance = importance;
		this.supplements = supplements;
		this.submission = null;
		this.problem = null;
	}

	private static String replaceMarkup(String source, String tag, String replacement) {
		return source.replaceAll("\\{"+tag+"\\}", Matcher.quoteReplacement(replacement));
	}
	
	private String getCleartextMessage(String message) {
		if (problem != null) {
			message = replaceMarkup(message, "problem", problem.getName());
		}
		if (team != null) {
			message = replaceMarkup(message, "team", submission.getTeam().getShortName());
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
