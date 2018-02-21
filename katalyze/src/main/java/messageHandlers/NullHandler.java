package messageHandlers;

import org.apache.log4j.Logger;

import legacyfeed.SimpleMessage;

public class NullHandler extends SingleMessageHandler {
	static Logger logger = Logger.getLogger(SingleMessageHandler.class);

	String tag; 
	
	public NullHandler(String tag) {
		super(tag);
	}

	@Override
	public void process(SimpleMessage message) {
		logger.debug(String.format("Ignoring message %s", message));
	}

}
