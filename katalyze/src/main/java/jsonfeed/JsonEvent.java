package jsonfeed;

import net.sf.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonEvent {
    private String id;
    private String type;
    private String op;
    private String time;
    private JSONObject data;
    static SimpleDateFormat timespanFormat = new SimpleDateFormat("H:mm:ss.SSS");

    public static JsonEvent from(JSONObject src) {
        JsonEvent target = new JsonEvent();
        target.id = src.getString("id");
        target.type = src.getString("type");
        target.op = src.getString("op");
        target.time = src.getString("time");
        target.data =src.getJSONObject("data");
        return target;

    }

    public String getString(String key) {
        return data.getString(key);
    }

    public int getInt(String key) {
        return data.getInt(key);
    }

    public long getTimespan(String key) {
        try {
        String timeString = data.getString(key);
        if (timeString == null) {
            return -1;
        }
            Date result = timespanFormat.parse(timeString);
            return result.toInstant().toEpochMilli();
        }
        catch (ParseException e) {
            return -1;
        }
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getOp() {
        return op;
    }

    public String getTime() {
        return time;
    }

    public String toString() {
        return String.format("[%s:%s]", type, id);
    }

}
