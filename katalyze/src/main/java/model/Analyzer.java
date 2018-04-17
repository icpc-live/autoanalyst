package model;

import io.EntityOperation;
import rules.*;
import icat.AnalystMessage;
import icat.AnalystMessageSource;

import java.util.*;

import org.apache.log4j.Logger;
import org.jfree.util.Log;

public class Analyzer implements NotificationTarget, EntityChangedHandler {
	
	final Contest contest;
	static Logger logger = Logger.getLogger(Analyzer.class);
	List<StandingsUpdatedEvent> stateRules = new ArrayList<>();
	List<SolutionSubmittedEvent> submissionRules = new ArrayList<>();
	List<EntityChangedHandler> entityChangedHandlers = new ArrayList<>();
	AnalystMessageSource analystMsgSource = null;
	
	List<NotificationTarget> targets = new ArrayList<NotificationTarget>();
	List<OutputHook> outputHooks = new ArrayList<OutputHook>();
	List<LifeCycleAware> lifeCycleAwareObjects = new ArrayList<LifeCycleAware>();
	
	JudgingOutcomes judgingOutcomes = new JudgingOutcomes();
	HashtagFinder hashtagFinder = new HashtagFinder();
	boolean stopped = false;

	
	int lastHookTime = -1;
	int videoCaptureTreshold;
	int nextEventId = 0;
	
	public Analyzer(Contest contest, int videoCaptureTreshold) {
		this.contest = contest;
		this.videoCaptureTreshold = videoCaptureTreshold;
	}
	
	private LoggableEvent buildEventFromAnalystMsg(AnalystMessage msg) {
		// Find team id
		
		String message = msg.text;
		Team firstTeam = null;
		
		List<String> teamsInMessage = hashtagFinder.teams(message);
		for (String teamTag : teamsInMessage) {
			Team team = hashtagFinder.getTeam(contest, teamTag);
			if (team != null) {
				if (firstTeam == null) {
					firstTeam = team;
				}
				message = message.replace(teamTag, team.getName());
			}
		}
		
		List<String> problemsInMessage = hashtagFinder.problems(message);
		for (String problemTag : problemsInMessage) {
			Problem problem = hashtagFinder.getProblem(contest, problemTag);
			if (problem != null) {
				message = message.replace(problemTag, problem.getNameAndLabel());
			}
		}

        Map supplements = new HashMap<String, String>();
        supplements.put("category", "human");

        // Ensure breaking events get treated as such. Treat all other events that come from here as
        // being manually entered by an analyst.
        EventImportance importance = (msg.priority == 0) ? EventImportance.Breaking : EventImportance.AnalystMessage;

        LoggableEvent newEvent = new LoggableEvent(
				contest,
				firstTeam,
				msg.contestTime*60000,
				message,
				importance, supplements);

		return newEvent;
	}
	
	public void forwardAnalystMessages() {
		if (analystMsgSource == null) {
			return;
		}
		
		try {
			List<AnalystMessage> newMessages = analystMsgSource.getNewMessages(contest.getMinutesFromStart());			
			for (AnalystMessage msg : newMessages) {
				notify(buildEventFromAnalystMsg(msg));				
			}
			
		}
		catch (Exception e) {
			Log.error(String.format("Failed to get new analyst messages when reading from icat Database: %s",e));
		}
		
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
	
	public void setAnalystMsgSource(AnalystMessageSource newSource) {
		this.analystMsgSource = newSource;
	}

	public void addEntityChangedHandler(EntityChangedHandler handler) {
		this.entityChangedHandlers.add(handler);
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
		stopped = true;
	}
	
	public void notify(LoggableEvent event) {
		for (NotificationTarget target : targets) {
			target.notify(event);
		}
	}
	
	
	public LoggableEvent createEvent(InitialSubmission submission, long contestTimeMillis, String message, EventImportance importance) {
		return new LoggableEvent(contest, contestTimeMillis, message, importance, submission, null);
	}
	
	public LoggableEvent createEvent(InitialSubmission submission, long contestTimeMillis, String message, EventImportance importance, Map<String,String> supplements) {
		return new LoggableEvent(contest, contestTimeMillis, message, importance, submission, supplements);
	}
	

	
	
	public void processRules(Standings before, Standings after, Judgement submission) {
		StandingsTransition transition = new StandingsTransition(this, before, after, submission);
		for (StandingsUpdatedEvent rule : stateRules) {
			rule.onStandingsUpdated(transition);
		}
	}

	public void contestStateChanged(ContestState oldState, ContestState newState) {
	    if (oldState.notStartedYet() && newState.isRunning()) {
	        notify(createEvent(null, 0, "Contest has started", EventImportance.Breaking));
        }

        if (oldState.isRunning() && newState.isRunning() && newState.isFrozen()) {
	        notify(createEvent(null, newState.frozenMillis-newState.startedMillis, "Scoreboard is now frozen until the end of the contest", EventImportance.Breaking));
        }

        if (oldState.isRunning() && newState.isEnded()) {
	        notify(createEvent(null, newState.endedMillis-newState.startedMillis, "Contest is now over", EventImportance.Breaking));
        }
    }

	public InitialSubmission submissionById(String id) {
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
				logger.error(String.format("Error %s while processing rule %s for judgement %s", e, rule, submission));
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
	
	public void publishStandings() {
		for (OutputHook hook : outputHooks) {
			if (hook instanceof StandingsPublisher) {
				((StandingsPublisher) hook).publishStandings();
			}
		}
	}

	public void notifyHooks(int minutesFromStart) {
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

	public boolean isStopped() {
	    return stopped;
    }
}

	