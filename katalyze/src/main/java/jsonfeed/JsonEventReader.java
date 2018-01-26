package jsonfeed;


import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class JsonEventReader {


    public ArrayList<JsonEvent> parse(InputStream input) {

        String[] result = new BufferedReader(new InputStreamReader(input))
                .lines().toArray(n -> new String[n]);

        return getJsonEvents(result);

    }

    public ArrayList<JsonEvent> parse(String data) {
        String[] entries = data.split("\n");
        return getJsonEvents(entries);
    }

    private ArrayList<JsonEvent> getJsonEvents(String[] entries) {
        ArrayList<JsonEvent> target = new ArrayList<>();
        for (String s : entries) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                JSONObject json = JSONObject.fromObject(trimmed);
                JsonEvent event = JsonEvent.from(json);
                target.add(event);
            }
        }
        return target;
    }

}
