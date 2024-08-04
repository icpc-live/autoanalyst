package model;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import web.EventVector;
import web.Publisher;
import web.StaticWebDocument;

import java.util.Iterator;

public class PublishableEventList {


	EventVector data = new EventVector();

	JsonArray jsonEvents = new JsonArray();
	final String url;
	final Publisher publisher;
	

	public PublishableEventList(Publisher publisher, String url) {
		this.url = url;
		this.publisher = publisher;
	}
		
	private void publish() {
		String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(jsonEvents);
		StaticWebDocument doc = new StaticWebDocument("application/json; charset=utf-8", jsonString);
		publisher.publish(url, doc);
	}
	
	public void clear() {
		jsonEvents = new JsonArray();
		publish();
	}

	public Iterator<LoggableEvent> iterator() {
		return data.iterator();
	}
	
	public void add(LoggableEvent event) {
		data.add(event);

		JsonObject eventInfo = getJsonObject(event);
		
		jsonEvents.add(eventInfo);
		publish();
		
	}

	private JsonObject getJsonObject(LoggableEvent event) {
		JsonObject eventInfo = new JsonObject();
		eventInfo.addProperty("id", Integer.toString(event.id));
		eventInfo.addProperty("time", Integer.toString(event.contestTimeMinutes()));
		eventInfo.addProperty("message", event.message);
		eventInfo.addProperty("importance", event.importance.ordinal());

		if (event.team != null) {
			eventInfo.addProperty("team", event.team.getId());
		}

		if (event.submission != null) {
			eventInfo.addProperty("judgement", event.submission.id);
		}
		return eventInfo;
	}


}
