package rules;

import java.util.ArrayList;
import katalyzeapp.DatabaseNotificationConfig;
import org.apache.log4j.Logger;
import model.*;

public abstract class StateComparingRuleBase {
	static Logger logger = Logger.getLogger(StateComparingRuleBase.class);
	
	private final ArrayList<NotificationTarget> targets = new ArrayList<NotificationTarget>();
	
	public void addNotificationTarget(NotificationTarget target) {
		targets.add(target);
	}
	
	void notify(LoggableEvent event) {
		if (event != null) {
			for (NotificationTarget target : targets) {
				try {
					target.notify(event);
				} catch (Exception e) {
					logger.error(String.format("Error notifying %s: %s", target, e));
				}
			}
		}
	}


	protected String rankString(int current, int previous) {
		if (current == 1) {
			if (previous == 1) {
				return "still leading the competition";
			} else {
				return "now leading the competition";
			}
		} else if (current == 2) {
			if (previous == 2) {
				return "still the runner-up";
			} else {
				return "now the runner-up";
			}
		} else {
			return String.format("at rank %d", current);
		}
	}

	protected String futureRankString(int current, int previous, boolean isInitialState) {
		if (current == 1) {
			if (previous == 1) {
				if (isInitialState) {
					return "solve the first problem and take the lead";
				} else {
					return "further extend their lead";
				}
			} else {
				return "lead the competition";
			}
		} else if (current == 2) {
			if (previous == 2) {
				return "still be the runner-up";
			} else {
				return "become the runner-up";
			}
		} else {
			return String.format("get rank %d", current);
		}
	}


	public String toString() {
		return this.getClass().getSimpleName();
	}

	

}
