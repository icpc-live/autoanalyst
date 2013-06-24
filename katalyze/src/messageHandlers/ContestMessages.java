package messageHandlers;

import model.Contest;
import io.SimpleMessage;

import java.util.*;
import org.apache.log4j.Logger;

public class ContestMessages {
	static Logger logger = Logger.getLogger(ContestMessages.class);
	
	ArrayList<MessageHandler> handlers = new ArrayList<MessageHandler>();
	Contest contest = new Contest();
	
	public void add(MessageHandler handler) throws Exception {
		handler.connectTo(contest);
		handlers.add(handler);
	}
	
	public ContestMessages(Contest contest) throws Exception {
		this.contest = contest;
		
		add(new ProblemHandler());
		add(new RunHandler());
		add(new TeamHandler());
		add(new TestCaseHandler());
		add(new NullHandler("clar"));
	}
	
	public void process(SimpleMessage message) {
				
		for (MessageHandler handler : handlers) {
			if (handler.supports(message)) {
				try {
					handler.process(message);
				} catch (Exception e) {
					logger.error(String.format("Message hander %s raised an error processing %s", handler, message));
					e.printStackTrace();
				}
			}
		}
	}
	
	public void close() {
		for (MessageHandler handler : handlers) {
			try {
				handler.close();
			} catch (Exception e) {
				logger.error(String.format("Message hander %s raised an error during close(): %s", handler, e));
				e.printStackTrace();
			}
		}
	}
	
	

}
