package model;

import io.EntityOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public void upsert(EntityOperation op, T entry) {
        switch (op) {
            case CREATE:
                add(entry);
                break;
            case UPDATE:
                add(entry);
                break;
            case DELETE:
                delete(entry.getId());
                break;
        }
    }

    public int size() {
        return data.size();
    }

    public ArrayList<T> getAll() {
        return new ArrayList<T>(data.values());
    }

}
