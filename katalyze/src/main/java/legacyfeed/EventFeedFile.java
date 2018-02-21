package legacyfeed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class EventFeedFile {
	
	final File targetFile;
	final FileOutputStream fileOutput;
	final PrintStream outputStream;
	Boolean closed = false;
	int length = 0;
	
	public EventFeedFile(File targetFile) throws FileNotFoundException, UnsupportedEncodingException {
		this.targetFile = targetFile;
		fileOutput = new FileOutputStream(targetFile);
		outputStream = new PrintStream(fileOutput,true, "UTF-8");
	}
	
	public InputStream getEventStream() throws FileNotFoundException {
		return new FileInputStream(targetFile);
	}
	
	public PrintStream getStream() {
		return outputStream;
	}
	
	public Boolean isClosed() {
		return closed;
	}
	
	public void close() throws IOException {
		outputStream.close();
		fileOutput.close();
		closed = true;
	}
	
	public int getLength() {
		return (int) targetFile.length();
	}

}


