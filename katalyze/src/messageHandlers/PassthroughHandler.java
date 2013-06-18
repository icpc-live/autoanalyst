package messageHandlers;

import java.io.PrintStream;

import javax.xml.stream.XMLStreamException;

import io.MessageXmlSerializer;
import io.SimpleMessage;
import model.Contest;

public class PassthroughHandler implements MessageHandler {

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

}
