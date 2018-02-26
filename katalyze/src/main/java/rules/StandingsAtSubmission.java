package rules;

import model.*;

public class StandingsAtSubmission {
	public final Standings before;
	public final InitialSubmission submission;
	private final Analyzer analyzer;
	
	public StandingsAtSubmission(Analyzer analyzer, Standings before, InitialSubmission submission) {
		this.analyzer = analyzer;
		this.before = before;
		this.submission = submission;
	}
	
	public LoggableEvent createEvent(String message, EventImportance importance) {
		return analyzer.createEvent(submission, submission.contestTimeMilliseconds, message, importance);
	}
	
	

}
