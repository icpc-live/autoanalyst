package io;

import org.apache.log4j.Logger;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;

public class TokenStreamProcessor {
	
	enum StreamState {Start, Middle, End};
	
	static Logger logger = Logger.getLogger(TokenStreamProcessor.class);


	Stack<Token> tokenStack;

	BlockingQueue<Token> input;
	Sink<SimpleMessage> output;

	Iterator<Token> iterator;
	Token current;
	
	
	public TokenStreamProcessor(BlockingQueue<Token> input, Sink<SimpleMessage> output) {
		this.input = input;
		this.output = output;
	}
	
	private Token advanceNextToken() throws InterruptedException {
		current = input.take(); // should hopefully block indefinitely
		logger.debug(String.format("advance -> %s", current));
		return current;
	}
	
	boolean currentIsWhiteSpace() {
		if (current instanceof TagValue) {
			TagValue value = (TagValue) current;
			return value.isWhiteSpace();
		} else {
			return false;
		}
	}
	
	private Token advanceIgnoringWhiteSpace() throws InterruptedException {
		do {
			advanceNextToken();
		} while (currentIsWhiteSpace());
		return current;
	}
	
	private void assertCurrentIs(Class<?> expectedClass) throws ParseException {
		if (current != null && expectedClass.isAssignableFrom(current.getClass())) {
			logger.debug(String.format("got %s as expected (%s)", current, expectedClass));
			return;
		} else {
			String valueClass = (current == null) ? "null" : String.format("%s (%s)", current, current.getClass());
			throw new ParseException(String.format("Expected to get %s but encountered %s", expectedClass, valueClass),0); 
		}
		
	}
	
	String parseValue() throws InterruptedException {
		String value = "";
		while (current instanceof TagValue) {
			TagValue token = (TagValue) current;
			value = value + token.getValue();
			
			advanceNextToken();
		}
		
		return value;
	}
	
	
	SimpleMessage parseSimpleMessage() throws Exception {
		assertCurrentIs(StartTag.class);	
		SimpleMessage target = new SimpleMessage(((StartTag)current).getName());
		advanceIgnoringWhiteSpace();
		
		while (current instanceof StartTag) {
			StartTag start = (StartTag) current;
			advanceIgnoringWhiteSpace();
			if (current instanceof TagValue) {
				String value = parseValue();
				target.put(start.getName(), value);
			} 
			assertCurrentIs(EndTag.class);
			advanceIgnoringWhiteSpace();
		}
		assertCurrentIs(EndTag.class);
		return target;
	}
	
	SimpleMessage controlMessage(String messageName) {
		return new SimpleMessage("!"+messageName);
	}

	
	public void parse() throws Exception {
		advanceIgnoringWhiteSpace();
		assertCurrentIs(StartTag.class);
		assert (current instanceof StartTag) && ((StartTag) current).getName() == "contest";
		output.send(controlMessage("beginStream"));

		advanceIgnoringWhiteSpace();
		while (current != null && !current.equals(EndOfStream.instance)) {
			if (current instanceof StartTag) {
				SimpleMessage message = parseSimpleMessage();
				output.send(message);
				advanceIgnoringWhiteSpace();
			} else if (current instanceof EndTag) {
				output.send(controlMessage("endStream"));
				break;
			}
		}
		
	}

}
