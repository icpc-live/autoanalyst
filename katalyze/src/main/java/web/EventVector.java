package web;

import model.LoggableEvent;

import java.util.ArrayList;
import java.util.Iterator;

public class EventVector {

    private final ArrayList<LoggableEvent> data = new ArrayList<>();
    private volatile int eventCount = 0;

    public synchronized void add(LoggableEvent item) {
        data.add(item);
        eventCount++;
    }

    public int size() {
        return eventCount;
    }

    public LoggableEvent get(int index) {
        return data.get(index);
    }

    public Iterator<LoggableEvent> iterator() {
        return new EventVectorIterator(0);
    }


    class EventVectorIterator implements Iterator<LoggableEvent> {
        int index;

        public EventVectorIterator(int initialIndex) {
            this.index = initialIndex;
        }

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public LoggableEvent next() {
            return get(index++);
        }
    }


}
