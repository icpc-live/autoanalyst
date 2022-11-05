package jsonfeed;


import io.Sink;
import net.sf.json.JSONObject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;

public class JsonEventReader {
    private static Logger log = LogManager.getLogger(JsonEventReader.class);
    private String lastProcessedId = null;
    private String lastProcessedToken = null;

    public ArrayList<JsonEvent> parse(Reader input) throws IOException {

        ArrayList<JsonEvent> target = new ArrayList<>();
        processStream(input, x -> target.add(x));
        return target;
    }

    public String getLastProcessedId() {
        return lastProcessedId;
    }

    public String getLastProcessedToken() {
        return lastProcessedToken;
    }

    public void processLine(String eventLine, Sink<JsonEvent> target) {
        String trimmedLine = eventLine.trim();
        if (!trimmedLine.isEmpty()) {
            try {
                JSONObject json = JSONObject.fromObject(trimmedLine);
                JsonEvent event = JsonEvent.from(json);
                lastProcessedId = event.getId();
                lastProcessedToken = json.optString("token", null);
                target.send(event);
            }
            catch (Exception e) {
                log.error(String.format("Failed to process event. Error: %s, Source: %s", e, trimmedLine));
            }
        }
    }


    public void processStream(Reader input, Sink<JsonEvent> target) throws IOException{
        BufferedReader reader = new BufferedReader(input);
        String eventLine;

        boolean scoreboardsFlushed = false;
        while (true) {

            if (reader.ready() || scoreboardsFlushed) {
                eventLine = reader.readLine();
                if (eventLine != null) {
                    processLine(eventLine, target);
                    scoreboardsFlushed = false;
                } else {
                    break;
                }
            } else {
                target.send(null);
                scoreboardsFlushed = true;
            }
        }

    }

    public ArrayList<JsonEvent> parse(String data) throws IOException {

        StringReader reader = new StringReader(data);

        return parse(reader);
    }
}
