package web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;


public class DynamicFileWebDocument implements WebDocument {
	File sourceFile;
	String contentType;

	public DynamicFileWebDocument(String contentType, String fileName) {
		this.sourceFile = new File(fileName);
		this.contentType = contentType;
		
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public void writeContents(OutputStream target) throws IOException {
		byte[] contents = new byte[(int) sourceFile.length()];
		FileInputStream fis = new FileInputStream(sourceFile);
		fis.read(contents);
		fis.close();

		target.write(contents);
	}

	@Override
	public boolean isGzipCompressed() {
		return false;
	}	
	
	
}
