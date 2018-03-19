package model;

import java.util.HashMap;
import java.util.Map;

public class EntityMap<T extends ApiEntity> {
    private final Map<String, T> data = new HashMap<>();

    public void add(T entry) {
        data.put(entry.getId(), entry);
    }

    public T get(String id) {
        return data.get(id);
    }

    public void delete(String id) {
        data.remove(id);
    }

    public int size() {
        return data.size();
    }

}
