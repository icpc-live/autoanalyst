package web;

import java.util.concurrent.ConcurrentHashMap;


public class WebPublisher implements Publisher {
	ConcurrentHashMap<String, WebDocument> documents = new ConcurrentHashMap<String, WebDocument>();
	boolean useCompression = false;
	
	public WebPublisher(boolean useCompression) {
		this.useCompression = useCompression;
	}
	
	public void publish(String url, WebDocument doc) {
		if (useCompression && !doc.isGzipCompressed()) {
			doc = CompressedWebDocument.Compress(doc);
		}
		documents.put(url, doc);
	}
	
	public WebDocument get(String url) {
		return documents.get(url);
	}
	

}
