package io;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface InputStreamProvider {

    InputStream getInputStream(String resumePoint, boolean isStreamToken) throws IOException;
}
