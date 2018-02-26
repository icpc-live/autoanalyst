package jsonfeed;

import net.sf.json.JSONObject;
import org.omg.CORBA.DynAnyPackage.Invalid;

import java.io.InvalidObjectException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonEvent {
    private String id;
    private String type;
    private String op;
    private JSONObject data;
    private static TimeConverter converter = new TimeConverter();

    public static JsonEvent from(JSONObject src) throws InvalidObjectException {
        JsonEvent target = new JsonEvent();
        target.id = src.getString("id");
        target.type = src.getString("type");
        target.op = src.getString("op");
        target.data =src.getJSONObject("data");

        if (target.data == null || target.data.isNullObject()) {
            throw new InvalidObjectException(String.format("Event %s does not contain a data element", src));
        }
        return target;

    }

    public String getString(String key) {
        return data.getString(key);
    }

    public int getInt(String key) {
        return data.getInt(key);
    }

    public boolean getBoolean(String key) {
        return data.getBoolean(key);
    }

    public long getTimespan(String key) {
        String timeString = data.getString(key);
        return converter.parseContestTimeMillis(timeString);
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

    public String toString() {
        return String.format("[%s:%s]", type, id);
    }

}