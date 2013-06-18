package web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class CompressedWebDocument implements WebDocument {
	final String contentType;
	final byte[] compressedContent;

	
	static WebDocument Compress(WebDocument source) {
		if (source.isGzipCompressed()) {
			return source;
		}
		
		return new CompressedWebDocument(source.getContentType(), compress(source));
		
	}
	
	public CompressedWebDocument(String contentType, byte[] compressedContent) {
		this.contentType = contentType;
		this.compressedContent = compressedContent;
	}
	
    private static byte[] compress(WebDocument source){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try{
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            source.writeContents(gzipOutputStream);
            gzipOutputStream.close();
            byteArrayOutputStream.flush();            
        } catch(IOException e){
        		throw new RuntimeException(e);
        }

        return byteArrayOutputStream.toByteArray();
    }	

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public void writeContents(OutputStream target) throws IOException {
		target.write(compressedContent);
	}

	@Override
	public boolean isGzipCompressed() {
		return true;
	}
}
