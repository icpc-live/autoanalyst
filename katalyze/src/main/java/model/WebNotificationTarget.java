package model;

import java.util.HashMap;

import web.Publisher;



public class WebNotificationTarget implements NotificationTarget {

    final Publisher publisher;
	
	PublishableEventList allEvents;
	HashMap<Team, PublishableEventList> teamEvents = new HashMap<Team, PublishableEventList>();

	public WebNotificationTarget(Publisher publisher) {
		this.publisher = publisher;
		this.allEvents = new PublishableEventList(publisher, "/AllNotifications");
	}
	
	
	public void reset() {
		allEvents.clear();
		teamEvents.clear();
	}

	public PublishableEventList getAllEvents() {
		return allEvents;
	}
	
	@Override
	public void notify(LoggableEvent event) {
		allEvents.add(event);

		if (event.team != null) {
			PublishableEventList teamList = teamEvents.get(event.team);
			if (teamList == null) {
				teamList = new PublishableEventList(publisher, "/TeamNotifications/"+event.team.getTeamId());
				teamEvents.put(event.team,teamList);
			}
			teamList.add(event);	
		}
	}

	

}
