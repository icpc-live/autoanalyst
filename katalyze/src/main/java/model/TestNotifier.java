package model;

import java.util.*;

public class TestNotifier implements NotificationTarget {
	List<String> items = new ArrayList<String>();

	@Override
	public void notify(LoggableEvent event) {
		String stringToAdd = String.format("%s %d %s", event.team.toString(), event.time, event.message.toString());
		items.add(stringToAdd);
	}
	
	public Boolean containsFragment(String fragment) {
		for (String s : items) {
			if (s.contains(fragment)) {
				return true;
			}
		}
		return false;
	}

}
