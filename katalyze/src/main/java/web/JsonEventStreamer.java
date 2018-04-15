package web;

import legacyfeed.EventFeedFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import model.LoggableEvent;
import model.PublishableEventList;

@SuppressWarnings("restriction")
public class JsonEventStreamer<T> implements WebHandler {

    final PublishableEventList entries;
    final String path;
    final EventSerializer<LoggableEvent> serializer;

    public JsonEventStreamer(PublishableEventList entries, EventSerializer<LoggableEvent> serializer, String path) {

        this.entries = entries;
        this.path = path;
        this.serializer = serializer;
    }

    @Override
    public boolean matches(HttpExchange exchange) {
        return (exchange.getRequestMethod().equalsIgnoreCase("GET")) &&
                exchange.getRequestURI().getPath().equalsIgnoreCase(path);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException{
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", "application/x-ndjson; charset=utf-8");
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        responseHeaders.set("Transfer-Encoding", "chunked");
        exchange.sendResponseHeaders(200, 0);

        OutputStream responseBody = exchange.getResponseBody();
        Writer writer = new OutputStreamWriter(responseBody, StandardCharsets.UTF_8);

        Iterator<LoggableEvent> iterator = entries.iterator();
        boolean eof = false;

        try {
            while (!eof) {
                if (iterator.hasNext()) {
                    LoggableEvent entry = iterator.next();

                    if (entry == null) {
                        eof = true;
                    } else {
                        serializer.write(entry, writer);
                    }
                } else {
                    writer.flush();
                    Thread.sleep(500);
                }
            }
        }
        catch (InterruptedException e) {
            writer.flush();
        }
        responseBody.close();
    }

}
