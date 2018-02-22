package model;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import web.EventSerializer;
import web.EventVector;
import web.Publisher;
import web.StaticWebDocument;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PublishableEventList {


	EventVector data = new EventVector();

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

	public Iterator<LoggableEvent> iterator() {
		return data.iterator();
	}
	
	public void add(LoggableEvent event) {
		data.add(event);

		JSONObject eventInfo = getJsonObject(event);
		
		jsonEvents.add(eventInfo);
		publish();
		
	}

	private JSONObject getJsonObject(LoggableEvent event) {
		JSONObject eventInfo = new JSONObject()
		.element("id", Integer.toString(event.id))
		.element("time", Integer.toString(event.time))
		.element("message", event.message)
		.element("importance", event.importance.ordinal());

		if (event.team != null) {
			eventInfo =  eventInfo.element("team", event.team.getTeamId());
		}

		if (event.submission != null) {
			eventInfo = eventInfo.element("submission", event.submission.id);
		}
		return eventInfo;
	}


}
