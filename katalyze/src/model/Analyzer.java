package model;

import rules.*;
import java.util.*;


import org.apache.log4j.Logger;

public class Analyzer implements NotificationTarget {
	
	final Contest contest;
	static Logger logger = Logger.getLogger(Analyzer.class);
	List<StandingsUpdatedEvent> stateRules = new ArrayList<StandingsUpdatedEvent>();
	List<SolutionSubmittedEvent> submissionRules = new ArrayList<SolutionSubmittedEvent>();
	
	List<NotificationTarget> targets = new ArrayList<NotificationTarget>();
	List<OutputHook> outputHooks = new ArrayList<OutputHook>();
	List<LifeCycleAware> lifeCycleAwareObjects = new ArrayList<LifeCycleAware>();
	
	JudgingOutcomes judgingOutcomes = new JudgingOutcomes();
	
	int lastHookTime = -1;
	int videoCaptureTreshold;
	int nextEventId = 0;
	
	public Analyzer(Contest contest, int videoCaptureTreshold) {
		this.contest = contest;
		this.videoCaptureTreshold = videoCaptureTreshold;
	}
	
	public void addRule(Object newRule) {
		if (newRule instanceof StateComparingRuleBase) {
			((StateComparingRuleBase) newRule).addNotificationTarget(this);
		}
		
		
		if (newRule instanceof StandingsUpdatedEvent) {
			stateRules.add((StandingsUpdatedEvent) newRule);
		} else if (newRule instanceof SolutionSubmittedEvent) {
			submissionRules.add((SolutionSubmittedEvent) newRule);
		} else {
			logger.error(String.format("Rule %s is not known to the Analyzer and will never be invoked", newRule));
			return;
		}
		logger.info(String.format("Rule %s activated", newRule));

	}
	
	public void addNotifier(NotificationTarget newNotifier) {
		targets.add(newNotifier);
	}
	
	public void manageLifeCycle(LifeCycleAware target) {
		lifeCycleAwareObjects.add(target);
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
	}
	
	public void notify(LoggableEvent event) {
		for (NotificationTarget target : targets) {
			target.notify(event);
		}
	}
	
	
	public LoggableEvent createEvent(InitialSubmission submission, String message, EventImportance importance) {
		return new LoggableEvent(contest, message, importance, submission, null);		
	}
	
	public LoggableEvent createEvent(InitialSubmission submission, String message, EventImportance importance, Map<String,String> supplements) {
		return new LoggableEvent(contest, message, importance, submission, supplements);
	}
	

	
	
	private void processRules(Standings before, Standings after, Submission submission) {
		StandingsTransition transition = new StandingsTransition(this, before, after, submission);
		for (StandingsUpdatedEvent rule : stateRules) {
			rule.onStandingsUpdated(transition);
		}
	}

	public void notifySubmission(Submission newSubmission) {
		int submissionSerial = newSubmission.getSerialNumber();
		Standings before = contest.getStandings(submissionSerial);
		Standings after = contest.getStandings(submissionSerial+1);
		processRules(before, after, newSubmission);
		notifyHooks(newSubmission.minutesFromStart);
	}
	
	public InitialSubmission submissionById(int id) {
		return judgingOutcomes.getSubmission(id);
	}
	
	public void freshSubmission(InitialSubmission submission) {
		judgingOutcomes.newSubmission(submission);
		Standings before = contest.getStandings();
		StandingsAtSubmission standings = new StandingsAtSubmission(this, before, submission);
		for (SolutionSubmittedEvent rule : submissionRules) {
			try {
				rule.onSolutionSubmitted(standings);
			}
			catch (Exception e) {
				logger.error(String.format("Error %s while processing rule %s for submission %d", e, rule, submission));
			}
		}
		notifyHooks(submission.minutesFromStart);
		
	}
	
	public void testCaseExecuted(TestCaseExecution outcome) {
		judgingOutcomes.testCaseRun(outcome);
	}
	
	public TestCaseExecution getFailureInfo(InitialSubmission submission) {
		return judgingOutcomes.getFailureInfo(submission);
	}
	

	private void notifyHooks(int minutesFromStart) {
		while (lastHookTime < minutesFromStart) {
			lastHookTime++;
			
			for(OutputHook hook : outputHooks) {
				hook.execute(lastHookTime);
			}
		}

	}

	public void addOutputHook(OutputHook outputHook) {
		outputHooks.add(outputHook);
	}
	
	public List<OutputHook> getOutputHooks() {
		return new ArrayList<OutputHook>(outputHooks);
	}
	

}

	