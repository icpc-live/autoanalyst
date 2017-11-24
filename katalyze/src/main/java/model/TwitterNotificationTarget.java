package model;

import config.TwitterConfig;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterNotificationTarget implements NotificationTarget {

	private static final Logger logger = LogManager.getLogger(TwitterNotificationTarget.class);
	private final String hashTag;
	private final int suppressedMinutes;
	private final Twitter twitter;

	public TwitterNotificationTarget(TwitterConfig config) {
		twitter = config.createTwitterInstance();
		hashTag = config.getHashtag();
		suppressedMinutes = config.getSuppressUntilMinutes();

		try {
			twitter.verifyCredentials();
		} catch (TwitterException e) {
			throw new RuntimeException(String.format("Failed to setup Twitter notifier: %s", e.getMessage()));
		}
	}

	@Override
	public void notify(LoggableEvent event) {
		if (event.time < suppressedMinutes) {
			return;
		}

		try {
			if (event.importance == EventImportance.Breaking) {
				String fullMessage = event.message + " "+ hashTag;
				if (fullMessage.length() > 140) {
					int maxContentLength = 140 - hashTag.length() - 1 - 3;
					
					fullMessage = event.message.substring(0, maxContentLength-1) + "... "+ hashTag;
				}
				logger.info(String.format("Tweeting: %s", fullMessage));
				twitter.updateStatus(fullMessage);
			}
		}
		catch (Exception e) {
			logger.warn(String.format("Failed to tweet: %s. Reason %s", event.toString(), e.getMessage()));
		}
	}
	
}
