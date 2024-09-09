package jsonfeed;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.EntityOperation;
import model.JsonHelpers;

import java.io.InvalidObjectException;

public class JsonEvent {
    private String id;
    private String type;
    private String op_str;
    private EntityOperation op;
    private JsonObject data;
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

    public static JsonEvent from(JsonObject src) throws InvalidObjectException {
        JsonEvent target = new JsonEvent();
        target.id = JsonHelpers.optString(src, "id");
        target.type = src.getAsJsonPrimitive("type").getAsString();
        target.op_str = JsonHelpers.optString(src, "op", "create");
        target.op = opFromStr(target.op_str);

        target.data =src.getAsJsonObject("data");

        if (target.id == null && !target.type.equals("state")) {
            throw new InvalidObjectException(String.format("Events of type %s must contain an ID field", target.type));
        }

        if (target.data == null) {
            throw new InvalidObjectException(String.format("Event %s does not contain a data element", src));
        }

	if (target.data.isJsonNull()) {
	    target.op = opFromStr("delete");
	}
        return target;
    }


    public String getString(String key) {
        return data.getAsJsonPrimitive(key).getAsString();
    }

    public String[] getStringArray(String key) {
        JsonArray entries = data.getAsJsonArray(key);
        if (entries == null) {
            return new String[0];
        }

        String[] target = new String[entries.size()];
        for (int i = 0; i<entries.size(); i++) {
            target[i] = entries.get(i).getAsString();
        }
        return target;
    }


    public String[] getUrlArray(String key) {
        if (!data.has(key)) {
            return new String[0];
        }
        JsonArray entries = data.getAsJsonArray(key);
        if (entries == null) {
            return new String[0];
        }

        String[] target = new String[entries.size()];
        for (int i = 0; i<entries.size(); i++) {
            JsonObject urlObject = entries.get(i).getAsJsonObject();
            target[i] = urlObject.getAsJsonPrimitive("href").getAsString();
        }
        return target;



    }

    public String getStringOrNull(String key) {
        return JsonHelpers.optString(data, key);
    }

    public int getInt(String key) {
        return data.getAsJsonPrimitive(key).getAsInt();
    }

    public boolean getBoolean(String key) {
        return data.getAsJsonPrimitive(key).getAsBoolean();
    }

    public boolean tryGetBoolean(String key, boolean defaultValue) {
        if (data.has(key)) {
            return getBoolean(key);
        } else {
            return defaultValue;
        }
    }

    public long getTimespan(String key) {
        String timeString = getString(key);
        return converter.parseContestTimeMillis(timeString);
    }

    public long getTimestamp(String key) {
        String timeStampString = getStringOrNull(key);
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

    public JsonObject getRawData() {
        return data;
    }

    public String toString() {
        return String.format("[%s:%s]", type, id);
    }

}
