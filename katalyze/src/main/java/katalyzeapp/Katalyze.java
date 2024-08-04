package katalyzeapp;

import config.YAMLConfiguration;
import io.HttpFeedClient;
import io.InputStreamConfigurator;
import io.InputStreamProvider;
import jsonfeed.JsonEventReader;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class Katalyze {
	
	static Logger logger = Logger.getLogger(Katalyze.class);




	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		System.setProperty("StartTime", Long.toString(Instant.now().toEpochMilli()/1000L));
		DOMConfigurator.configure("log4j.xml");

		logger.info("Katalyzer started");

		String fileName = null;
		String configFileName = null;
		Configuration config = null;
		
		for (int i = 0; i < args.length; ++i) {
			if ("-input".equals(args[i]) && i<args.length-1){
				fileName = args[i+1];
				i++;
			} else if ("-config".equals(args[i]) && i<args.length-1) {
				configFileName = args[i+1];
				i++;
			} else {
				logger.warn("I don't understand argument " + args[i]);
			}
		}

		if (configFileName == null) {
			configFileName = "config.yaml";
		}

		try {
			config = new YAMLConfiguration(configFileName);
		} catch (ConfigurationException e) {
			logger.error(String.format("Error while parsing %s: %s.\nCause: %s", configFileName, e.getMessage(), e.getCause()));
		}

		final Katalyzer katalyzer = new Katalyzer(config);
		katalyzer.start();

		InputStreamConfigurator configSource = new InputStreamConfigurator(config);

		InputStreamProvider isp;
		if (fileName == null) {
			isp = configSource.getInputFromConfig();
		} else {
			isp = configSource.createFileReader(fileName);
		}

		try {

			if (config.getString("CDS.protocol", "contestapi").equalsIgnoreCase("contestapi")) {
				JsonEventReader reader = new JsonEventReader();
				boolean isFirst = true;
                while (!katalyzer.isStopped()) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        logger.info(String.format("Waiting 10 seconds before reconnecting to the source and resuming after %s",
                                reader.getLastProcessedId()));
                        Thread.sleep(10000);
                    }


                    try {
						String sinceToken = reader.getLastProcessedToken();
						boolean isStreamToken;
						if (sinceToken == null) {
							sinceToken = reader.getLastProcessedId();
							isStreamToken = false;
						} else {
							isStreamToken = true;
						}

						Runnable continuousUpdateTask = () -> { 
							while (!Thread.currentThread().isInterrupted()) {
								katalyzer.updateScoreboards(false);
								try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									Thread.currentThread().interrupt();
              						return;
								}
							}
						};

						Thread continuousUpdateThread = new Thread(continuousUpdateTask);
						continuousUpdateThread.start();

						try {
							InputStream input = isp.getInputStream(sinceToken, isStreamToken);
                        	reader.processStream(new InputStreamReader(input, StandardCharsets.UTF_8), katalyzer::processEvent);
						} finally {
							continuousUpdateThread.interrupt();
							continuousUpdateThread.join();
						}
                    }
                    catch (IOException e) {
                        logger.info(String.format("Error while reading from source: %s", e));
                    }
                }
			} else {
				katalyzer.processLegacyFeed(isp.getInputStream(null, false));
			}

			logger.info("Katalyzer stream finished");

			System.in.read();

		} catch (Exception e) {
			logger.error("Katalyzer fatal error, terminating",e);
		}


		if (katalyzer != null) {
			katalyzer.stop();
		}
		
	}


}


