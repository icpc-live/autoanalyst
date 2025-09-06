package model;

import io.EntityOperation;
import org.icpclive.cds.api.ContestStatus;
import rules.*;

import java.time.Instant;
import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Analyzer implements NotificationTarget, EntityChangedHandler {
	
	final Contest contest;
	static Logger logger = LogManager.getLogger(Analyzer.class);
	List<StandingsUpdatedEvent> stateRules = new ArrayList<>();
	List<SolutionSubmittedEvent> submissionRules = new ArrayList<>();
	List<EntityChangedHandler> entityChangedHandlers = new ArrayList<>();

	List<NotificationTarget> targets = new ArrayList<NotificationTarget>();
	List<OutputHook> outputHooks = new ArrayList<OutputHook>();
	List<LifeCycleAware> lifeCycleAwareObjects = new ArrayList<LifeCycleAware>();
	
	JudgingOutcomes judgingOutcomes = new JudgingOutcomes();
	boolean stopped = false;

	
	int lastHookTime = -1;
	
	public Analyzer(Contest contest, int videoCaptureTreshold) {
		this.contest = contest;
	}


	public void addRule(Object newRule) {
        logger.error(String.format("Rule %s is not known to the Analyzer and will never be invoked", newRule));
	}

    public void start() {
		for (LifeCycleAware target : lifeCycleAwareObjects) {
			try {
				target.start();
			} catch (Exception e) {
				logger.error(String.format("Error while starting %s: %s", target, e));
			}
		}
	}

	public void stop() {
		for (LifeCycleAware target : lifeCycleAwareObjects) {
			try {
				target.stop();
			} catch (Exception e) {
				logger.error(String.format("Error while stopping %s: %s", target, e));
			}
		}
		stopped = true;
	}
	
	public void notify(LoggableEvent event) {
		for (NotificationTarget target : targets) {
			target.notify(event);
		}
	}
	
	
	public LoggableEvent createEvent(InitialSubmission submission, long contestTimeMillis, String message, EventImportance importance) {
		return new LoggableEvent(contest, contestTimeMillis, (submission == null ? Instant.now() : submission.timestamp), message, importance, submission, null);
	}
	
	public LoggableEvent createEvent(InitialSubmission submission, long contestTimeMillis, String message, EventImportance importance, Map<String,String> supplements) {
		return new LoggableEvent(contest, contestTimeMillis, (submission == null ? Instant.now() : submission.timestamp), message, importance, submission, supplements);
	}
	

	
	
	public void processRules(Standings before, Standings after, Judgement submission) {
		StandingsTransition transition = new StandingsTransition(this, before, after, submission);
		if (!submission.getTeam().isHidden()) {
			for (StandingsUpdatedEvent rule : stateRules) {
				rule.onStandingsUpdated(transition);
			}
		}
	}

	public void contestStateChanged(ContestStatus oldState, ContestStatus newState) {
    }


	public void freshSubmission(InitialSubmission submission) {

		judgingOutcomes.newSubmission(submission);

		if (submission.getTeam().isHidden()) {
			return;
		}

		Standings before = contest.getStandings();
		StandingsAtSubmission standings = new StandingsAtSubmission(this, before, submission);
		for (SolutionSubmittedEvent rule : submissionRules) {
			try {
				rule.onSolutionSubmitted(standings);
			}
			catch (Exception e) {
				logger.error(String.format("Error %s while processing rule %s for judgement %s", e, rule, submission));
			}
		}
		notifyHooks(submission.minutesFromStart);
		
	}
	

	public TestCaseExecution getFailureInfo(InitialSubmission submission) {
		return judgingOutcomes.getFailureInfo(submission);
	}


	public void notifyHooks(int minutesFromStart) {
		while (lastHookTime < minutesFromStart) {
			lastHookTime++;
			
			for(OutputHook hook : outputHooks) {
				hook.execute(lastHookTime);
			}
		}

	}


	@Override
	public void entityChanged(ApiEntity entity, EntityOperation op) {
		for (EntityChangedHandler handler : entityChangedHandlers) {
			try {
				handler.entityChanged(entity, op);
			}
			catch (Exception e) {
				logger.warn(String.format("Entity changed handler %s failed for entity %s, operation %s: %s", handler, entity, op.toString(), e));
			}
		}
	}

}

	
