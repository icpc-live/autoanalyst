package model;

public class ConsoleNotifier implements NotificationTarget {
	
	final boolean useHashTags;

	public ConsoleNotifier(boolean useHashTags) {
		this.useHashTags = useHashTags;
	}

	public void notify(LoggableEvent event) {
		String message = (useHashTags) ? event.icatMessage : event.message;
		
		System.out.println(String.format("%s, [%n] %s", event.team, event.time, message));
	}
}
