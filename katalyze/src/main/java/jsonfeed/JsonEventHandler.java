package jsonfeed;

import io.EntityOperation;
import model.Contest;

@FunctionalInterface
public interface JsonEventHandler {
    void process(Contest contest, JsonEvent src) throws Exception;
}
