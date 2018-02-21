package jsonfeed;


import io.Sink;
import net.sf.json.JSONObject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;

public class JsonEventReader {
    private static Logger log = LogManager.getLogger(JsonEventReader.class);

    public ArrayList<JsonEvent> parse(Reader input) throws IOException {

        ArrayList<JsonEvent> target = new ArrayList<>();
        processStream(input, x -> target.add(x));
        return target;
    }


    public void processLine(String eventLine, Sink<JsonEvent> target) {
        String trimmedLine = eventLine.trim();
        if (!trimmedLine.isEmpty()) {
            try {
                JSONObject json = JSONObject.fromObject(trimmedLine);
                JsonEvent event = JsonEvent.from(json);
                target.send(event);
            }
            catch (Exception e) {
                log.error(String.format("Failed to processLegacyFeed event. Error: %s, Source: %s", e, trimmedLine));
            }
        }
    }


    public void processStream(Reader input, Sink<JsonEvent> target) throws IOException{
        BufferedReader reader = new BufferedReader(input);
        String eventLine;
        while ((eventLine = reader.readLine()) != null) {
            processLine(eventLine, target);
        }
    }

    public ArrayList<JsonEvent> parse(String data) throws IOException {

        StringReader reader = new StringReader(data);

        return parse(reader);
    }
}
