package web;

import java.io.IOException;
import java.io.OutputStream;


public interface WebDocument {
	
	String getContentType();
	void writeContents(OutputStream target) throws IOException;
	boolean isGzipCompressed();
	
}