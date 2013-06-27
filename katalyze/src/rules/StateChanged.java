package rules;

import model.EventImportance;
import model.NotificationTarget;

public class StateChanged extends StateComparingRuleBase implements StandingsUpdatedEvent{
	private StandingsCriterion criterion;
	
	public StateChanged(NotificationTarget target, StandingsCriterion criterion) {
		super(target);
		this.criterion = criterion;
	}

	@Override
	public void onStandingsUpdated(StandingsTransition transition) {
		boolean fulfilledBefore = criterion.isFulfilled(transition.before);
		if (!fulfilledBefore) {
			boolean fulfilledAfter = criterion.isFulfilled(transition.after);
			if (fulfilledAfter) {
				notify(transition.createEvent(criterion.message(), EventImportance.Normal));
			}
		}
		
	}
	
	
}
