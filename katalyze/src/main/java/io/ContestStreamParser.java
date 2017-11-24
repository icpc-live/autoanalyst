package io;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
 
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public class ContestStreamParser {
	static final Logger logger = Logger.getLogger(ContestStreamParser.class);
	
	SAXParser parser;
	Sink<Token> output;
	
	public void open(InputStream input, Sink<Token> output) throws ParserConfigurationException, SAXException, IOException {
			
		SAXParserFactory factory = SAXParserFactory.newInstance();   
		parser = factory.newSAXParser();
		DefaultHandler handler = new ParseHandler();
		this.output = output;
		
		try {
			parser.parse(input, handler);
		}
		catch (SAXException e) {
			if (e instanceof SAXParseException && e.getMessage().contains("must start and end within the same entity")) {
				logger.warn("Read input all the way to the end of the stream. This is probably ok unless we are reading directly from Kattis...");
			}
		}

	}
	
	void sendToken(Token token) {
		output.send(token);
	}
	
	
	class ParseHandler extends DefaultHandler {
		 
	     public void startElement(String uri, String localName,
	    		 String qName, Attributes attributes) throws SAXException {
	    	 

	    	 sendToken(new StartTag(qName));
	    	 
	     }
	 
	     public void endElement(String uri, String localName,
	          String qName)
	          throws SAXException {
	    	 
	    	 sendToken(new EndTag(qName));
	    	 
	     }
	 
	     public void characters(char ch[], int start, int length)
	         throws SAXException {
	    	 
	    	 String value = new String(ch, start, length);
	    	 String trimmedValue = value;
	    	 if (trimmedValue.length() > 0) {
	    		 sendToken(new TagValue(trimmedValue));
	    	 }
	     }
	}

}
