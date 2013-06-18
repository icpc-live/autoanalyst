package model;

import org.apache.log4j.*;

import config.TwitterConfig;

import twitter4j.*;

public class TwitterNotificationTarget implements NotificationTarget {

	static Logger logger = LogManager.getLogger(TwitterNotificationTarget.class);
	String hashtag;
	
	Twitter twitter;
	
	public TwitterNotificationTarget(TwitterConfig config) {
		twitter = config.createTwitterInstance();
		hashtag = config.getHashtag();
	}
	
	@Override
	public void notify(LoggableEvent event) {
		try {
			if (event.importance == EventImportance.Breaking) {
				String fullMessage = event.message + " "+hashtag;
				if (fullMessage.length() > 140) {
					int maxContentLength = 140 - hashtag.length() - 1 - 3;
					
					fullMessage = event.message.substring(0, maxContentLength-1) + "... "+hashtag;
				}
				
				twitter.updateStatus(fullMessage);
			}
		}
		catch (Exception e) {
			logger.warn(String.format("Failed to tweet: %s. Reason %s", event.toString(), e.getMessage()));
		}
	}
	

}
