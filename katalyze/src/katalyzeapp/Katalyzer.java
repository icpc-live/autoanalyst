package katalyzeapp;

import messageHandlers.ContestMessages;
import model.Analyzer;
import model.Contest;
import model.LogNotificationTarget;
import io.*;

import java.io.*;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.log4j.Logger;

public class Katalyzer {
	static Logger logger = Logger.getLogger(Katalyzer.class);
	int updateInterval = 2000;
	long lastUpdate = 0;
	
	Contest contest;
	ContestMessages handlers;
	
	public Katalyzer(BaseConfiguration config) throws Exception {
		this.contest = new Contest();
		this.handlers = new ContestMessages(contest);		
		
		Analyzer analyzer = contest.getAnalyzer();
		
		ConfigReader configReader = new ConfigReader(config);
		configReader.SetupAnalyzer(contest, analyzer, handlers);

		analyzer.addNotifier(new LogNotificationTarget(false));
		handlers.addStandardHandlers();

	}
	
	
	private void updateScoreboards(boolean force) {
		long currentTime = System.currentTimeMillis();
		if (force || currentTime - lastUpdate > updateInterval) {
			contest.getAnalyzer().publishStandings();
			contest.getAnalyzer().forwardAnalystMessages();
			lastUpdate = currentTime;
		}
	}
	
	
	public void process(InputStream stream) throws Exception {
		logger.info(String.format("Processing stream of type %s", stream.getClass()));
		TokenFeeder feeder = new TokenFeeder(stream);
		
		TokenQueue tokenQueue = new TokenQueue(feeder.getQueue());

		
		
		while (tokenQueue.isOpen()) {
			SimpleMessage message = tokenQueue.pop(500);
			if (message != null) {
				double contestTime = message.tryGetDouble("time", Double.NaN);
				if (!Double.isNaN(contestTime)) {
					contest.updateTime(contestTime);
				}
				handlers.process(message);
				updateScoreboards(false);
			}						
		}
		// Ok, we're done. Push the final standings.
		updateScoreboards(true);
		
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


}
