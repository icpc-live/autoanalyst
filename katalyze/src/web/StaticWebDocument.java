package web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

public class StaticWebDocument implements WebDocument {
	final String contentType;
	final byte[] contents;
	
	
	public StaticWebDocument(String contentType, String contents) {
		this(contentType, contents.getBytes(Charset.forName("UTF-8")));
	}
	
	public StaticWebDocument(String contentType, byte[] rawBytes) {
		this.contentType = contentType;
		this.contents = rawBytes;
	}
	
	
	public static StaticWebDocument FromFile(String contentType, String fileName) throws IOException{
		
		File sourceFile = new File(fileName);
		byte[] contents = new byte[(int) sourceFile.length()];
		FileInputStream fis = new FileInputStream(sourceFile);
		fis.read(contents);
		fis.close();
		
		return new StaticWebDocument(contentType, contents);	
	}

	public static StaticWebDocument FromResource(String contentType, java.lang.Object mainObject, String fileName) throws IOException{
		InputStream is = mainObject.getClass().getResourceAsStream(fileName);
		if (is == null) {
			return FromFile(contentType, fileName);
		}
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = is.read(data, 0, data.length)) != -1) {
		  buffer.write(data, 0, nRead);
		}

		buffer.flush();
	
		return new StaticWebDocument(contentType, buffer.toByteArray());	
	}
	

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public void writeContents(OutputStream target) throws IOException {
		target.write(contents);
	}

	@Override
	public boolean isGzipCompressed() {
		return false;
	}

}
