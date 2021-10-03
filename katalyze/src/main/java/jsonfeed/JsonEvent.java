package jsonfeed;

import io.EntityOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.InvalidObjectException;

public class JsonEvent {
    private String id;
    private String type;
    private String op_str;
    private EntityOperation op;
    private JSONObject data;
    private static TimeConverter converter = new TimeConverter();

    private static EntityOperation opFromStr(String str) {
        switch (str) {
            case "create":
                return EntityOperation.CREATE;
            case "update":
                return EntityOperation.UPDATE;
            case "delete":
                return EntityOperation.DELETE;
            default:
                return EntityOperation.UNDEFINED;

        }
    }

    public static JsonEvent from(JSONObject src) throws InvalidObjectException {
        JsonEvent target = new JsonEvent();
        target.id = src.getString("id");
        target.type = src.getString("type");
        target.op_str = src.getString("op");
        target.op = opFromStr(target.op_str);

        target.data =src.getJSONObject("data");

        if (target.data == null || target.data.isNullObject()) {
            throw new InvalidObjectException(String.format("Event %s does not contain a data element", src));
        }
        return target;

    }


    public String getString(String key) {
        return data.getString(key);
    }

    public String[] getStringArray(String key) {
        JSONArray entries = data.getJSONArray(key);
        if (entries == null) {
            return new String[0];
        }

        String[] target = new String[entries.size()];
        for (int i = 0; i<entries.size(); i++) {
            target[i] = entries.getString(i);
        }
        return target;
    }


    public String[] getUrlArray(String key) {
        JSONArray entries = data.getJSONArray(key);
        if (entries == null) {
            return new String[0];
        }

        String[] target = new String[entries.size()];
        for (int i = 0; i<entries.size(); i++) {
            JSONObject urlObject = entries.getJSONObject(i);
            target[i] = urlObject.getString("href");
        }
        return target;



    }

    public String getStringOrNull(String key) {
        return data.has(key) ? data.getString(key) : null;
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

    public long getTimestamp(String key) {
        String timeStampString = data.optString(key, null);
        return converter.parseTimestampMillis(timeStampString);
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public EntityOperation getOp() {
        return op;
    }

    public String getOpStr() {
        return op_str;
    }

    public JSONObject getRawData() {
        return data;
    }

    public String toString() {
        return String.format("[%s:%s]", type, id);
    }

}
