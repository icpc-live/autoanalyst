package katalyzeapp;

import legacyfeed.SimpleMessage;
import legacyfeed.TokenFeeder;
import legacyfeed.TokenQueue;
import jsonfeed.JsonEvent;
import jsonfeed.JsonEventHandler;
import jsonfeed.StandardEventHandlers;
import messageHandlers.ContestMessages;
import model.Analyzer;
import model.Contest;
import io.LogNotificationTarget;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import java.io.InputStream;

public class Katalyzer {
	private static Logger logger = Logger.getLogger(Katalyzer.class);
	int updateInterval = 2000;
	StandardEventHandlers eventHandlers;
	long lastUpdate = 0;


	
	Contest contest;
	ContestMessages handlers;
	
	public Katalyzer(Configuration config) throws Exception {
		ConfigReader configReader = new ConfigReader(config);
		this.contest = new Contest();
		this.handlers = new ContestMessages(contest);		
		
		Analyzer analyzer = contest.getAnalyzer();
		eventHandlers = new StandardEventHandlers();
		
		configReader.SetupAnalyzer(contest, analyzer, handlers);

		analyzer.addNotifier(new LogNotificationTarget(false));
		handlers.addStandardHandlers(configReader.getConnection());
	}
	
	
	public synchronized void updateScoreboards(boolean force) {
	    if (force) {
	        logger.debug("Forced scoreboard update");
        }
		long currentTime = System.currentTimeMillis();
		if (force || currentTime - lastUpdate > updateInterval) {
			contest.getAnalyzer().publishStandings();
			contest.getAnalyzer().forwardAnalystMessages();
			lastUpdate = currentTime;
		}
	}
	
	
	public void processLegacyFeed(InputStream stream) throws Exception {
		logger.info(String.format("Processing stream of type %s", stream.getClass()));
		TokenFeeder feeder = new TokenFeeder(stream);
		
		TokenQueue tokenQueue = new TokenQueue(feeder.getQueue());
		
		while (tokenQueue.isOpen()) {
			SimpleMessage message = tokenQueue.pop(500);
			if (message != null) {
				double contestTime = message.tryGetDouble("time", Double.NaN);
				if (!Double.isNaN(contestTime)) {
					contest.updateTime((long) Math.floor(contestTime*1000));
				}
				handlers.process(message);
			}
			updateScoreboards(false);
		}
		// Ok, we're done. Push the final standings.
		updateScoreboards(true);
		
	}

	public synchronized void processEvent(JsonEvent event)  {
	    if (event != null) {
            JsonEventHandler eventHandler = eventHandlers.getHandlerFor(event);
            try {
                eventHandler.process(contest, event);
            } catch (Exception e) {
                logger.error(String.format("Error %s while processing event %s", e, event), e);
            }
            updateScoreboards(false);
        } else {
	        updateScoreboards(true);
        }

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

	public boolean isStopped() {
		return contest.getAnalyzer().isStopped();
	}


}
