package config;

import java.security.InvalidParameterException;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class TwitterConfig {
	String[] oAuthConsumerKeys;
	String[] oAuthAccessToken;
	String hashtag;
	
	public TwitterConfig(String[] oAuthConsumerKeys, String[] oAuthAccessToken, String hashtag) {
		if (oAuthConsumerKeys.length != 2) {
			throw new InvalidParameterException("invalid oAuthConsumerKeys array");
		}
		if (oAuthAccessToken.length != 2) {
			throw new InvalidParameterException("invalid oAuthAccessToken array");
		}
		this.oAuthConsumerKeys = oAuthConsumerKeys;
		this.oAuthAccessToken = oAuthAccessToken;
		this.hashtag = (hashtag == null) ? "" : hashtag;
	}
	
	public Twitter createTwitterInstance() {
		Twitter twitter = new TwitterFactory().getInstance();
	    twitter.setOAuthConsumer(oAuthConsumerKeys[0], oAuthConsumerKeys[1]);
	    AccessToken accessToken = new twitter4j.auth.AccessToken(oAuthAccessToken[0], oAuthAccessToken[1]);
	    twitter.setOAuthAccessToken(accessToken);
	    
	    return twitter;
	}
	
	public String getHashtag() {
		return this.hashtag;
	}

}
