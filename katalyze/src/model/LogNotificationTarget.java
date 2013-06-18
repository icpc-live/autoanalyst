package model;

import org.apache.log4j.Logger;

public class LogNotificationTarget implements NotificationTarget {
	
	static Logger logger = Logger.getLogger(LogNotificationTarget.class);
	final boolean useHashTags;
	
	public LogNotificationTarget(boolean useHashTags) {
		this.useHashTags = useHashTags;
	}

	@Override
	public void notify(LoggableEvent event) {
		String messageText = useHashTags ? event.icatMessage : event.message;
		
		String fullMessage = String.format("[%d] %s", event.time, messageText);
		if (event.importance.ordinal() <= EventImportance.Normal.ordinal()) {
			logger.info(fullMessage);
		} else {
			logger.debug(fullMessage);
		}
	}
	

}
