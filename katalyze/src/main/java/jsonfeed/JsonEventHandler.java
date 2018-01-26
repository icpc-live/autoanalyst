package jsonfeed;

import model.Contest;

@FunctionalInterface
public interface JsonEventHandler {
    void process(Contest contest, JsonEvent src) throws Exception;
}
