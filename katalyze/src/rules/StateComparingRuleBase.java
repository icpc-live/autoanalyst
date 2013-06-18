package rules;

import model.*;

public abstract class StateComparingRuleBase implements StandingsUpdatedEvent {
	
	protected final NotificationTarget target;
	
	public StateComparingRuleBase(NotificationTarget target) {
		this.target = target;
	}
	

	
	public abstract void onStandingsUpdated(StandingsTransition transition);

}
