package rules;

import java.util.*;
import model.*;

public abstract class StateComparingRule {
	
	final private List<NotificationTarget> notificationTargets = new ArrayList<NotificationTarget>();
	
	public void addNotificationTarget(NotificationTarget notificationTarget) {
		notificationTargets.add(notificationTarget);
	}
	
	private String getCleartextMessage(String message, Submission submission) {
		if (submission != null) {
			message = message.replaceAll("\\{problem\\}", submission.getProblem().getName());
			message = message.replaceAll("\\{team\\}", submission.getTeam().getName());
		}
		return message;
	}
	
	private String getICatMessage(String message, Submission submission) {
		if (submission != null) {
			message = message.replaceAll("\\{problem\\}", submission.getProblem().toString());
			message = message.replaceAll("\\{team\\}", submission.getTeam().toString());
		}
		return message;
	}
	
	
	protected void notify(Submission submission, String message, EventImportance importance) {
		for (NotificationTarget target : notificationTargets) {
			int minutesFromStart = submission.getMinutesFromStart();
			Team team = submission.getTeam();
			Score score = team.getScore(submission.getSerialNumber() + 1);

			String clearTextMessage = getCleartextMessage(message, submission);
			String icatMessage = getICatMessage(message, submission);
			
			LoggableEvent event = new LoggableEvent(minutesFromStart, team.getContest(), submission.getTeam(), minutesFromStart, clearTextMessage, icatMessage, score, importance, submission.getInitialSubmission());
			target.notify(event);
		}
	}
	
	public abstract void process(Standings before, Standings after, Submission submission);

}
