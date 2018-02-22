package web;

import legacyfeed.EventFeedFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction") 
public class EventFeedStreamer implements WebHandler {
	
	final EventFeedFile eventDump;
	final String path;
	
	public EventFeedStreamer(EventFeedFile eventDump, String path) {

		this.eventDump = eventDump;
		this.path = path;
	}

	@Override
	public boolean matches(HttpExchange exchange) {
		return (exchange.getRequestMethod().equalsIgnoreCase("GET")) &&
		exchange.getRequestURI().getPath().equalsIgnoreCase(path);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException{
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", "text/xml; charset=utf-8");
		responseHeaders.set("Access-Control-Allow-Origin", "*");
		responseHeaders.set("Transfer-Encoding", "chunked");
		exchange.sendResponseHeaders(200, 0);
		
		OutputStream responseBody = exchange.getResponseBody();		
		
		InputStream streamFile = eventDump.getEventStream();		
		byte[] buffer= new byte[16384];
		Boolean eof = false;
		Writer writer = new OutputStreamWriter(responseBody);
		int bytesWritten = 0;
		try {
			while (!eof) {
				int bytesRead = streamFile.read(buffer, 0, buffer.length);
			    if (bytesRead > 0) {
			    	responseBody.write(buffer, 0, bytesRead);
			    	bytesWritten += bytesRead;
			    } else {
			    	responseBody.flush();
			    	if (eventDump.isClosed() && bytesWritten == eventDump.getLength()) {
			    		eof = true;
			    	} else {
			    		Thread.sleep(500);
			    	}
			    }
				
			}
		}
		catch (InterruptedException e) {
			writer.write("Interrupted!");
			writer.flush();
		}
		responseBody.close();

	}

}
