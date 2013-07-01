package model;

import java.io.IOException;

import org.apache.log4j.Logger;

public class ShellNotificationTarget implements NotificationTarget {
	static Logger logger = Logger.getLogger(ShellNotificationTarget.class);

	String template;
	Runtime runtime;
	
	public ShellNotificationTarget(String template) {
		this.template = template;
		this.runtime = Runtime.getRuntime();
	}
	
	
	@Override
	public void notify(LoggableEvent event) {
		String command = substituteTags(event);
		try {
			logger.debug(String.format("Executing: %s", command));
			runtime.exec(command);
		} catch (IOException e) {
			logger.error(String.format("Error executing '%s':%s", command, e));
		}
	}




	private String substituteTags(LoggableEvent event) {
		String output = template;
		
		if (event.team != null) {
			output = output.replace("{teamId}", Integer.toString(event.team.getTeamNumber()));
		}
		
		if (event.submission != null) {
			output = output.replace("{runId}", Integer.toString(event.submission.id));
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
