package messageHandlers;

import java.io.PrintStream;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import io.MessageXmlSerializer;
import io.SimpleMessage;
import model.Contest;
import model.InitialSubmission;
import model.LoggableEvent;
import model.NotificationTarget;

public class PassthroughHandler implements MessageHandler, NotificationTarget {
	static Logger logger = Logger.getLogger(PassthroughHandler.class);

	PrintStream output;
	MessageXmlSerializer serializer;
	
	
	public PassthroughHandler(PrintStream output) throws XMLStreamException {
		this.output = output;
		serializer = new MessageXmlSerializer(output);
	}
	
	@Override
	public void connectTo(Contest contest) {
		// do nothing.
	}

	@Override
	public Boolean supports(SimpleMessage message) {
		return true;
	}
	

	@Override
	public void close() throws XMLStreamException {
		serializer.close();
	}
	

	@Override
	public void process(SimpleMessage message) throws XMLStreamException {
		String messageName = message.getName();
		if ("!endStream".equals(messageName)){
			serializer.close();
		} else if ("!beginStream".equals(messageName)) {
			serializer.init();
		} else {
			serializer.serialize(message);
		}
	}
	
	private SimpleMessage createFeedMessageFromEvent(LoggableEvent event) {
		SimpleMessage eventMessage = new SimpleMessage("analystmsg");
		eventMessage.put("id", Integer.toString(event.id));
		eventMessage.put("team", Integer.toString(event.team.getTeamNumber()));
		eventMessage.put("time", Integer.toString(event.time));
		if (event.submission != null) {
			InitialSubmission submission = event.submission;
			eventMessage.put("problem", submission.problem.getId());
			eventMessage.put("run_id", Integer.toString(submission.getId()));
		}
		eventMessage.put("message", event.message);
		return eventMessage;		
	}

	@Override
	public void notify(LoggableEvent event) {
		try {
			serializer.serialize(createFeedMessageFromEvent(event));
		}
		catch (Exception e) {
			logger.error(String.format("Error while posting event to xml feed: %s", e));
			// If there is an error, there isn't much more we can do than log it and hope that someone notices.
			// The show must go on.
		}

	}

}
