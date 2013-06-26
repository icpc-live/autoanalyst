package model;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import web.Publisher;
import web.StaticWebDocument;

public class PublishableEventList {
	
	JSONArray jsonEvents = new JSONArray();
	final String url;
	final Publisher publisher;
	

	public PublishableEventList(Publisher publisher, String url) {
		this.url = url;
		this.publisher = publisher;
	}
		
	private void publish() {
		String jsonString = jsonEvents.toString(2);
		StaticWebDocument doc = new StaticWebDocument("application/json; charset=utf-8", jsonString);
		publisher.publish(url, doc);
	}
	
	public void clear() {
		jsonEvents.clear();
		publish();
	}
	
	public void add(LoggableEvent event) {
		JSONObject eventInfo = new JSONObject()
		.element("id", Integer.toString(event.id))
		.element("team", event.team.getTeamNumber())
		.element("time", Integer.toString(event.time))
		.element("message", event.message)
		.element("importance", event.importance.ordinal());
		
		if (event.submission != null) {
			eventInfo = eventInfo.element("submission", event.submission.id);
		}
		
		jsonEvents.add(eventInfo);
		publish();
		
	}
	
	

}
