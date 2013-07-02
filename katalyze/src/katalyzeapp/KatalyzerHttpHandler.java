package katalyzeapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import web.*;

import model.Contest;
import model.LifeCycleAware;

import com.sun.net.httpserver.*;

@SuppressWarnings("restriction")
class KatalyzerHttpHandler implements HttpHandler, LifeCycleAware {
	static Logger logger = Logger.getLogger(Katalyze.class);
	
	Contest contest;
	HttpServer server = null;
	WebPublisher publisher;
	int port;
	

	public KatalyzerHttpHandler(Contest contest, WebPublisher publisher, int port) {
		this.contest = contest;
		this.publisher = publisher;
		this.port = port;
	}
	
	public void start() throws IOException{
	    InetSocketAddress addr = new InetSocketAddress(port);
	    server = HttpServer.create(addr, 0);

	    server.createContext("/", this);
	    server.setExecutor(Executors.newCachedThreadPool());
	    server.start();
	    logger.info(String.format("HTTP listener started on port %d", port));
	    
	    publisher.publish("/scoreboard.html", new DynamicFileWebDocument("text/html","scoreboard.html"));
	    publisher.publish("/scoreboard", new DynamicFileWebDocument("text/html","scoreboard.html"));
	    
	    publisher.publish("/web/scores.js", new DynamicFileWebDocument("text/javascript","web/scores.js"));
	    publisher.publish("/web/jquery-1.6.1.js", StaticWebDocument.FromResource("text/javascript", this, "web/jquery-1.6.1.js"));
	    publisher.publish("/web/jquery-ui-1.8.13.custom.js", StaticWebDocument.FromResource("text/javascript", this, "web/jquery-ui-1.8.13.custom.js"));
	    publisher.publish("/css/katalyze.css", new DynamicFileWebDocument("text/css","css/katalyze.css"));
	    
	}
	
	
	
	
	public void stop() {
		server.stop(0);
	    logger.info("HTTP listener stopped");
	}
	
	
	private void sendWebDocument(HttpExchange exchange, WebDocument doc) throws IOException {
		
			Headers responseHeaders = exchange.getResponseHeaders();

			responseHeaders.set("Content-Type", doc.getContentType());
			responseHeaders.set("Access-Control-Allow-Origin", "*");
			if (doc.isGzipCompressed()) {
				responseHeaders.set("Content-Encoding", "gzip");
			}
			exchange.sendResponseHeaders(200, 0);
			
			OutputStream responseBody = exchange.getResponseBody();
			
			doc.writeContents(responseBody);
			responseBody.close();
		
	}
	
	
	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		if (requestMethod.equalsIgnoreCase("GET")) {
			
			URI uri = exchange.getRequestURI();
			String path = uri.getPath();
			logger.debug(String.format("%s %s (%s)", requestMethod, path, exchange.getRemoteAddress().toString()));
			WebDocument document = this.publisher.get(path);
			if (document != null) {
				sendWebDocument(exchange, document);;
			} else {
				logger.warn("404: "+uri);
				Headers responseHeaders = exchange.getResponseHeaders();
				responseHeaders.set("Content-Type", "text/plain");
				responseHeaders.set("Access-Control-Allow-Origin", "*");
				exchange.sendResponseHeaders(404, 0);
				
				OutputStream responseBody = exchange.getResponseBody();
				Headers requestHeaders = exchange.getRequestHeaders();
				Set<String> keySet = requestHeaders.keySet();
				Iterator<String> iter = keySet.iterator();
				while (iter.hasNext()) {
					String key = iter.next();
					List<?> values = requestHeaders.get(key);
					String s = key + " = " + values.toString() + "\n";
					responseBody.write(s.getBytes());
				}
				responseBody.close();
				
			}
			
			

		}
	}
}
