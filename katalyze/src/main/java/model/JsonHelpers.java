
package model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonHelpers {
    public static String optString(JsonObject object, String key, String default_value) {
        JsonElement p = object.get(key);
        if (p == null || p.isJsonNull()) {
            return default_value;
        }
        return p.getAsString();
    }

    public static String optString(JsonObject object, String key) {
        return optString(object, key, null);
    }

    public static JsonArray toJsonArray(String[] ar) {
        JsonArray result = new JsonArray();
        for (String p: ar) {
            result.add(p);
        }
        return result;
    }
}
