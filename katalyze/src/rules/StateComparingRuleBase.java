package rules;

import java.util.ArrayList;
import katalyzeapp.DatabaseNotificationConfig;
import org.apache.log4j.Logger;
import model.*;

public abstract class StateComparingRuleBase {
	static Logger logger = Logger.getLogger(DatabaseNotificationConfig.class);
	
	private final ArrayList<NotificationTarget> targets = new ArrayList<NotificationTarget>();
	
	public void addNotificationTarget(NotificationTarget target) {
		targets.add(target);
	}
	
	void notify(LoggableEvent event) {
		for (NotificationTarget target : targets) {
			try {
				target.notify(event);
			}
			catch (Exception e) {
				logger.error(String.format("Error notifying %s: %s", target, e));
			}
		}
	}
	

}
