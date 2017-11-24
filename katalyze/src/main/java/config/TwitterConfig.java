package config;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import java.security.InvalidParameterException;

public class TwitterConfig {
	private final String[] oAuthConsumerKeys;
	private final String[] oAuthAccessToken;
	private final String hashtag;
	private final int suppressUntilMinutes;
	
	public TwitterConfig(String[] oAuthConsumerKeys, String[] oAuthAccessToken, String hashtag, int suppressUntilMinutes) {
		if (oAuthConsumerKeys.length != 2) {
			throw new InvalidParameterException("invalid oAuthConsumerKeys array");
		}
		if (oAuthAccessToken.length != 2) {
			throw new InvalidParameterException("invalid oAuthAccessToken array");
		}
		this.oAuthConsumerKeys = oAuthConsumerKeys;
		this.oAuthAccessToken = oAuthAccessToken;
		this.hashtag = (hashtag == null) ? "" : hashtag;
		this.suppressUntilMinutes = suppressUntilMinutes;
	}
	
	public Twitter createTwitterInstance() {
		Twitter twitter = new TwitterFactory().getInstance();
	    twitter.setOAuthConsumer(oAuthConsumerKeys[0], oAuthConsumerKeys[1]);
	    AccessToken accessToken = new AccessToken(oAuthAccessToken[0], oAuthAccessToken[1]);
	    twitter.setOAuthAccessToken(accessToken);
	    
	    return twitter;
	}
	
	public String getHashtag() {
		return this.hashtag;
	}

	public int getSuppressUntilMinutes() {
		return suppressUntilMinutes;
	}
}
