package rules;

import model.*;

public abstract class StateComparingRuleBase {
	
	private final NotificationTarget target;
	
	public StateComparingRuleBase(NotificationTarget target) {
		this.target = target;
	}
	
	void notify(LoggableEvent event) {
		target.notify(event);
	}
	

}
