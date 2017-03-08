package katalyzeapp;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.github.mbredel.commons.configuration.*;

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
		try {
			InputStream input;
		
			if (fileName == null) {
				input = System.in;
			} else {
				input = new FileInputStream(fileName);
			}
			
			katalyzer = new Katalyzer(config);
			katalyzer.start();
			katalyzer.process(input);


		} catch (Exception e) {
			logger.error(e,e);
		}
		finally {
            logger.info("Done. Press enter to stop the application");

            int c = System.in.read();
            while (c != 10) {
                c = System.in.read();
            }

			if (katalyzer != null) {
				katalyzer.stop();
			}
			
			logger.info("Katalyze terminated");
		}
		
	}

}


