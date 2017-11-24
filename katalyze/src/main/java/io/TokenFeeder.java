package io;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class TokenFeeder implements Sink<Token> {
	
	InputStream input;
	ContestStreamParser parser;
	BlockingQueue<Token> tokenQueue;
	Sink<Token> sink;
	Thread parseThread = null;
	
	public TokenFeeder(InputStream input) {
		this.tokenQueue = new LinkedBlockingQueue<Token>();
		parser = new ContestStreamParser();
		sink = this;
		this.input = input;
	}
	
	public BlockingQueue<Token> getQueue() {
		if (parseThread != null) {
			return tokenQueue;
		}
		
		parseThread = new Thread("parse") {
			public void run() {
				try {
					parser.open(input, sink);
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tokenQueue.add(EndOfStream.instance);
			}
		};

		parseThread.start();
		
		return tokenQueue;
	}
	
	public void send(Token token) {
		this.tokenQueue.add(token);
	}

}
