package katalyzeapp;

import messageHandlers.ContestMessages;
import model.Analyzer;
import model.Contest;
import model.LogNotificationTarget;
import model.WebNotificationTarget;
import io.*;

import java.io.*;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import teamscore.ExtendedScoreDump;
import web.FileWebPublisher;
import web.WebPublisher;

public class Katalyzer implements Sink<SimpleMessage> {
	static Logger logger = Logger.getLogger(Katalyze.class);
	
	Contest contest;
	ContestMessages handlers;
	KatalyzerHttpHandler httpHandler;
	WebPublisher webPublisher;
	int httpPort;
	
	public Katalyzer() throws Exception {
		this.webPublisher = new WebPublisher();
		this.contest = new Contest();
		this.handlers = new ContestMessages(contest);
		int port = 8099;
		
		if (port>0) {
			this.httpHandler = new KatalyzerHttpHandler(contest, webPublisher);
			this.httpPort = port;
		}
		
		
		Analyzer analyzer = contest.getAnalyzer();

		if (port > 0) {
			analyzer.addOutputHook(new ExtendedScoreDump(contest, webPublisher));
		}
		
		ConfigReader configReader = new ConfigReader(new PropertiesConfiguration("katalyzer.properties"));
		configReader.SetupAnalyzer(contest, analyzer, handlers);
		

		analyzer.addNotifier(new LogNotificationTarget(false));
		analyzer.addNotifier(new WebNotificationTarget(webPublisher));
		analyzer.addNotifier(new WebNotificationTarget(new FileWebPublisher("/Users/Stein/Dev/icat/bin/webRoot")));
	}
	
	public void process(InputStream stream) throws Exception {
		httpHandler.start(httpPort);
		logger.info(String.format("Processing stream of type %s", stream.getClass()));
		TokenFeeder feeder = new TokenFeeder(stream);
		BlockingQueue<Token> tokenQueue = feeder.getQueue();

		TokenStreamProcessor messageBuilder = new TokenStreamProcessor(tokenQueue, this);
		messageBuilder.parse();		
	}
	
	public Contest getContest() {
		return this.contest;
	}
	
	public void close() {
		httpHandler.stop();
	}

	
	@Override
	public void send(SimpleMessage message) {
		logger.debug(String.format("Process %s", message));
		handlers.process(message);
	}

}
