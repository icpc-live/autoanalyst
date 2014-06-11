package web;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public interface WebHandler {
	boolean matches(HttpExchange exchange);
	public void handle( HttpExchange exchange) throws IOException;
}
