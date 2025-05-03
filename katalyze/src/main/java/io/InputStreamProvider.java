package io;

import org.icpclive.cds.plugins.clics.ClicsFeed;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface InputStreamProvider {
    @NotNull ClicsFeed getClicsFeed();
    //InputStream getInputStream(String resumePoint, boolean isStreamToken) throws IOException;
}
