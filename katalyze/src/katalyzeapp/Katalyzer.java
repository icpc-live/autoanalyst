package katalyzeapp;

import messageHandlers.ContestMessages;
import model.Analyzer;
import model.Contest;
import model.LogNotificationTarget;
import io.*;

import java.io.*;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class Katalyzer implements Sink<SimpleMessage> {
	static Logger logger = Logger.getLogger(Katalyze.class);
	
	Contest contest;
	ContestMessages handlers;
	
	public Katalyzer() throws Exception {
		this.contest = new Contest();
		this.handlers = new ContestMessages(contest);		
		
		Analyzer analyzer = contest.getAnalyzer();
		
		ConfigReader configReader = new ConfigReader(new PropertiesConfiguration("katalyzer.properties"));
		configReader.SetupAnalyzer(contest, analyzer, handlers);

		analyzer.addNotifier(new LogNotificationTarget(false));

	}
	
	public void process(InputStream stream) throws Exception {
		logger.info(String.format("Processing stream of type %s", stream.getClass()));
		TokenFeeder feeder = new TokenFeeder(stream);
		BlockingQueue<Token> tokenQueue = feeder.getQueue();

		TokenStreamProcessor messageBuilder = new TokenStreamProcessor(tokenQueue, this);
		messageBuilder.parse();		
	}
	
	public Contest getContest() {
		return this.contest;
	}
	
	public void start() {
		contest.getAnalyzer().start();
	}
	
	public void stop() {
		contest.getAnalyzer().stop();
	}

	
	@Override
	public void send(SimpleMessage message) {
		logger.debug(String.format("Process %s", message));
		handlers.process(message);
	}

}
