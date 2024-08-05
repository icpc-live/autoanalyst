package web;

import java.io.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class FileWebPublisher implements Publisher {
	
	static final Logger logger = LogManager.getLogger(FileWebPublisher.class);
	
	String basePath;
	
	public FileWebPublisher(String basePath) throws FileNotFoundException {
		this.basePath = basePath;
		File pathFile = new File(basePath);
		
		if (!pathFile.exists()) {
			throw new FileNotFoundException(String.format("The location %s does not exist. Make sure it does!", basePath));
		}
		if (!pathFile.isDirectory()) {
			throw new FileNotFoundException(String.format("The location %s is not a directory", basePath));
		}
	}
	
	private String pathForDocument(String url) {
		String urlWithSeparator = (url.startsWith("/")) ? url : "/" + url;
		
		return basePath + urlWithSeparator.replace('/', File.separatorChar);
	}
	
	private void ensureParentDirectoryExists(File targetFile) {
		String parentFileName = targetFile.getParent();
		File parentDirectory = new File(parentFileName);
		if (!parentDirectory.exists()) {
			logger.info(String.format("Directory %s doesn't exist. Creating it!", parentDirectory));
			parentDirectory.mkdirs();
		}
	}

	@Override
	public void publish(String url, WebDocument doc) {
		
		String targetFileName = pathForDocument(url);
		
		String logString = String.format("publishing %s", targetFileName); 
		logger.debug(logString);
		
		File target = new File(targetFileName);
		
		ensureParentDirectoryExists(target);
		
		FileOutputStream targetFile = null;
		try {
			targetFile = new FileOutputStream(target);
			doc.writeContents(targetFile);
		} catch (IOException e) {
			logger.error(String.format("Unable to publish document to %s. Reason: %s:", targetFileName, e));
		}

		if (targetFile != null) {
			try {
				targetFile.close();
			} catch (IOException e) {
				logger.error(String.format("Error closing file %s: %s",  targetFileName, e));
			}
		}
		
	}

}