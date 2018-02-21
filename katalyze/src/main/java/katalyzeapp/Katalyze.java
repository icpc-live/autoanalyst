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
import java.io.InputStream;
import java.io.InputStreamReader;

public class Katalyze {
	
	static Logger logger = Logger.getLogger(Katalyze.class);




	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("log4j.xml");
		logger.info("Katalyze started");

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

		Katalyzer katalyzer = null;
		katalyzer = new Katalyzer(config);
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
				reader.processStream(new InputStreamReader(isp.getInputStream()), katalyzer::processEvent);
			} else {
				katalyzer.processLegacyFeed(isp.getInputStream());
			}

			logger.info("Katalyzer stream finished");

		} catch (Exception e) {
			logger.error("Katalyzer fatal error, terminating",e);
		}


		if (katalyzer != null) {
			katalyzer.stop();
		}
		
	}


}


