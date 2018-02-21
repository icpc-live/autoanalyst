package tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.*;
import io.*;

import legacyfeed.*;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

public class ParseTests {

	final String title = "The 2010 World Finals of the ACM International Collegiate Programming Contest";
		
	final String parseInput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
		"<contest>\n"+
		" <reset></reset>\n"+
		" <info>\n"+
		"  <length>05:00:00</length>\n"+
		"  <penalty>20</penalty>\n"+
		"  <started>False</started>\n"+
		"  <empty></empty>\n"+
		"  <starttime>0.0</starttime>\n"+
		"  <title>"+title+"</title>\n"+
		" </info>\n"+
		"</contest>";
	
	final Token[] expectedTokens = {
			new StartTag("contest"),
			new StartTag("reset"),
			new EndTag("reset"),
			new StartTag("info"),
			new StartTag("length"),
			new TagValue("05:00:00")
	};
	
	private InputStream getInputStream() {
		ByteArrayInputStream inputStream = new ByteArrayInputStream( parseInput.getBytes(Charset.forName("UTF-8")));
		return inputStream;
		
	}
	

	@Test
	public void testTokenParsing() {
		ContestStreamParser parser = new ContestStreamParser();
		
		Sink<Token> tokenSink = new Sink<Token>() {
			
			int index = 0;
			
			public void send(Token token) {
				if (index < expectedTokens.length) {

					if (token instanceof TagValue && ((TagValue) token).isWhiteSpace()) {
						return;
					} else {
						assertEquals(expectedTokens[index], token);
						index++;
					}
				}
			}
		};
		
		try {
			parser.open(getInputStream(), tokenSink);
		} catch (Exception e) {
		}
	}
	
	@Test
	public void testMessageParsing() throws Exception {
		BasicConfigurator.configure();
		TokenFeeder feeder = new TokenFeeder(getInputStream());
		BlockingQueue<Token> tokenQueue = feeder.getQueue();
		final ArrayList<SimpleMessage> result = new ArrayList<SimpleMessage>();
		
		Sink<SimpleMessage> simpleMessageSink = new Sink<SimpleMessage>() {
			@Override
			public void send(SimpleMessage message) {
				result.add(message);
			}
		};
		
		TokenStreamProcessor messageBuilder = new TokenStreamProcessor(tokenQueue, simpleMessageSink);
		messageBuilder.parse();
		
		SimpleMessage reset = result.get(1);
		SimpleMessage info = result.get(2);
		
		assertEquals("Reset message has no members", 0, reset.size());
		assertEquals("Info message contains title", title, info.get("title"));
	}
		

}
