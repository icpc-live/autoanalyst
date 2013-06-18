package katalyzeapp;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;


public class Katalyze {
	
	static Logger logger = Logger.getLogger(Katalyze.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure("log4j.xml");
		logger.info("Katalyze started");

		String fileName = null;
		
		for (int i = 0; i < args.length; ++i) {
			if ("-input".equals(args[i]) && i<args.length-1){
				fileName = args[i+1];
				i++;
			} else {
				logger.warn("I don't understand argument " + args[i]);
			}
		}
		
		Katalyzer katalyzer = null;
		try {
			InputStream input;
		
			if (fileName == null) {
				input = System.in;
			} else {
				input = new FileInputStream(fileName);
			}
			
			katalyzer = new Katalyzer();
			katalyzer.process(input);
			
			logger.info("Done. Press enter to stop the application");
			
			int c = System.in.read();
			while (c != 10) {
				c = System.in.read();
			}

		} catch (Exception e) {
			logger.error(e,e);
		}
		finally {
			if (katalyzer != null) {
				katalyzer.close();
			}
			
			logger.info("Katalyze terminated");
		}
		
	}

}


