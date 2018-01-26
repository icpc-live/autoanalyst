package model;

import java.io.IOException;

import org.apache.log4j.Logger;

public class ShellNotificationTarget implements NotificationTarget {
	static Logger logger = Logger.getLogger(ShellNotificationTarget.class);

	String template;
	Runtime runtime;
	int suppressedMinutes = 0;
	
	public ShellNotificationTarget(String template) {
		this.template = template;
		this.runtime = Runtime.getRuntime();
	}
	
	public void suppressUntil(int contestMinutes) {
		this.suppressedMinutes = contestMinutes;
	}
	
	
	@Override
	public void notify(LoggableEvent event) {
		if (event.time < suppressedMinutes) {
			return;
		}
		
		String command = substituteTags(event);
		try {
			logger.info(String.format("Executing: %s", command));
			runtime.exec(command);
		} catch (IOException e) {
			logger.error(String.format("Error executing '%s':%s", command, e));
		}
	}
	
	public static String escape(String source) {
		StringBuilder target = new StringBuilder();
		for (int i=0; i<source.length(); i++) {
			char c = source.charAt(i);
			if (Character.isLetter(c)) {
				target.append(c);
			} else if (Character.isWhitespace(c)) {
				target.append("_");
			}			
		}
		return target.toString();
	}

	private String substituteTags(LoggableEvent event) {
		String output = template;
		
		if (event.team != null) {
			output = output.replace("{teamId}", event.team.getTeamId());
		}
		
		if (event.submission != null) {
			output = output.replace("{runId}", event.submission.id);
			output = output.replace("{problemLetter}", event.submission.problem.getLetter());
			output = output.replace("{teamName}", escape(event.team.getName()));
		}
		
		output = output.replace("{time}", Integer.toString(event.time));
		if (event.supplements != null) {
			for (String key : event.supplements.keySet()) {
				String value = event.supplements.get(key);
			
				output = output.replace("{"+key+"}", value);
			}
		}
		
		return output;
	}

}
