package web;

import java.io.IOException;
import java.io.Writer;

public interface EventSerializer<T> {
    void write(T data, Writer output) throws IOException;

}
