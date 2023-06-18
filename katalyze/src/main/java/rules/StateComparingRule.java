package rules;

import java.time.Instant;
import java.util.*;
import model.*;

public abstract class StateComparingRule {
	
	final private List<NotificationTarget> notificationTargets = new ArrayList<NotificationTarget>();
	
	public void addNotificationTarget(NotificationTarget notificationTarget) {
		notificationTargets.add(notificationTarget);
	}
	
	protected void notify(Judgement submission, String message, EventImportance importance) {
		Team team = submission.getTeam();
		Instant timestamp = submission.getTeam().getContest().getStartTime().plusMillis(submission.getJudgementTimeMillis());
		LoggableEvent event = new LoggableEvent(team.getContest(), submission.getJudgementTimeMillis(), timestamp, message, importance, submission.getInitialSubmission(), null);

		for (NotificationTarget target : notificationTargets) {
			target.notify(event);
		}
	}
	
	public abstract void process(Standings before, Standings after, Judgement submission);

}
