package messageHandlers;

import io.SimpleMessage;
import model.Contest;

public interface MessageHandler {

	public abstract void connectTo(Contest contest) throws Exception;

	public abstract Boolean supports(SimpleMessage message);
	
	public abstract void close() throws Exception;
	
	public abstract void process(SimpleMessage message) throws Exception;

}