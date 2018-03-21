package messageHandlers;

import java.io.IOException;
import java.io.PrintStream;
import javax.xml.stream.XMLStreamException;

import model.*;
import org.apache.log4j.Logger;

import legacyfeed.EventFeedFile;
import legacyfeed.MessageXmlSerializer;
import legacyfeed.SimpleMessage;

public class PassthroughHandler implements MessageHandler, NotificationTarget {
	static Logger logger = Logger.getLogger(PassthroughHandler.class);

	PrintStream output;
	EventFeedFile eventFeedFile;
	MessageXmlSerializer serializer;
	
	
	public PassthroughHandler(EventFeedFile eventFeedFile) throws XMLStreamException {
		this.eventFeedFile = eventFeedFile;
		this.output = eventFeedFile.getStream();
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
	public void close() throws XMLStreamException, IOException {
		serializer.close();
		eventFeedFile.close();
	}

	@Override
	public void process(SimpleMessage message) throws XMLStreamException, IOException {
		String messageName = message.getName();
		if ("!endStream".equals(messageName)){
			close();
		} else if ("!beginStream".equals(messageName)) {
			serializer.init();
		} else {
			serializer.serialize(message);
		}
	}

    private int getEventImportanceNumber(EventImportance src) {
        switch (src) {
            case Breaking:
                return 0;
            case AnalystMessage:
                return 1;
            case Normal:
                return 2;
            case Whatever:
                return 3;
            default:
                // As of this writing, we should not end up here, but in case more
                // items are added to the enumeration, let them just have 'normal' priority until
                // we know...
                return 2;
        }
    }
	
	private SimpleMessage createFeedMessageFromEvent(LoggableEvent event) {
		SimpleMessage eventMessage = new SimpleMessage("analystmsg");
		eventMessage.put("id", Integer.toString(event.id));
		if (event.team != null) {
			eventMessage.put("team", event.team.getId());
		}
		eventMessage.put("time", Integer.toString(event.contestTimeMinutes()));
        eventMessage.put("priority", Integer.toString(getEventImportanceNumber(event.importance)));
		if (event.submission != null) {
			InitialSubmission submission = event.submission;
			eventMessage.put("problem", submission.problem.getId());
			eventMessage.put("run_id", submission.getId());
            eventMessage.put("submission", submission.getId());
		} else {
            eventMessage.put("submission", "-1");
        }

        String category = "auto";
        if (event.supplements != null) {
            String categorySupplement = event.supplements.get("category");
            if (categorySupplement != null) {
                category = categorySupplement;
            }
        }
        eventMessage.put("category", category);
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
