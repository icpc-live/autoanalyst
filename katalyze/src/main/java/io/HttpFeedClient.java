package io;

import com.google.gson.JsonParser;
import jsonfeed.ContestEntry;
import jsonfeed.DummyTrustManager;
import model.ContestProperties;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.function.Predicate;

public class HttpFeedClient {
    private static Logger log = LogManager.getLogger(HttpFeedClient.class);


    private String user = null;
    private String password = null;
    private boolean bypassCertificateErrors = false;


    public HttpFeedClient() { }

    public void setCredentials(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public void bypassCertErrors_DANGER(boolean enabled) {
        bypassCertificateErrors = enabled;
        if (bypassCertificateErrors) {
            log.warn("SSL Errors will be ignored. The connection can not be trusted. Don't use this setting for a real competition!");
        }
    }

    public JsonElement getJson(String urlString) throws IOException {
        InputStream is = getInputStream(urlString);
        String jsonString = IOUtils.toString(is, StandardCharsets.UTF_8);
        return JsonParser.parseString(jsonString);
    }


    public InputStream getInputStream(String urlString) throws IOException {
        HttpURLConnection connection = createConnection(urlString);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Got unexpected response code: "+Integer.toString(responseCode));
        }

        InputStream is = connection.getInputStream();
        return is;
    }


    public ArrayList<ContestEntry> probeContests(String baseUrl) throws IOException {
        ArrayList<ContestEntry> target = new ArrayList<>();

        JsonElement response = getJson(baseUrl);
        if (response.isJsonArray()) {
            // We got an array of contests;
            JsonArray responseArray = response.getAsJsonArray();
            responseArray.forEach(src -> {
                if (src.isJsonObject()) {
                    ContestProperties props = ContestProperties.fromJSON(src.getAsJsonObject());
                    ContestEntry newEntry = new ContestEntry(props, baseUrl+"/"+props.getId());
                    target.add(newEntry);
                }
            });
        } else {
            // Url pointed directly at one particular contest
            ContestProperties props = ContestProperties.fromJSON(response.getAsJsonObject());
            target.add(new ContestEntry(props, baseUrl));
        }

        return target;
    }

    public ContestEntry guessContest(String baseUrl, Predicate<ContestEntry> filter) throws IOException {
        ArrayList<ContestEntry> contests = probeContests(baseUrl);

        StringWriter candidates = new StringWriter();

        long now = Instant.now().toEpochMilli();

        ContestEntry bestMatch = null;
        contests.sort(Comparator.comparingLong(x -> x.getStartTime()));

        for (ContestEntry current : contests) {

            boolean isActiveNow = current.isActive(now);
            boolean isCandidate = filter.test(current);

            candidates.write(String.format("%s [%s - %s] active:%s isCandidate:%s\n", current, current.getStartTime(),
                    current.getEndTime(), Boolean.toString(isActiveNow), Boolean.toString(isCandidate)));

            if (isCandidate) {
                long timeToStart = current.getStartTime() - now;
                if (bestMatch == null
                        || current.isActiveWithinAnHour(now)
                        || (timeToStart < 0 && (current.getStartTime() > bestMatch.getStartTime()))) {
                    bestMatch = current;
                }
            }
        }

        log.info(String.format("Contest candidates:\n%s", candidates.toString()));

        return bestMatch;
    }


    private HttpURLConnection createConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        log.info(String.format("Connecting to %s", url));
        if (bypassCertificateErrors) {
            try {
                // configure the SSLContext with a TrustManager
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(new KeyManager[0], new TrustManager[]{new DummyTrustManager()}, new SecureRandom());
                SSLContext.setDefault(ctx);
            }
            catch (Exception e) {
                log.error("Error while disabling SSL errors: "+e.toString());
            }
        }


        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);

        if (bypassCertificateErrors && connection instanceof HttpsURLConnection) {
            HttpsURLConnection tlsConnection = (HttpsURLConnection) connection;
            tlsConnection.setHostnameVerifier((s, session) -> true);
        }

        if (user != null && password != null) {
            String userPassword = user + ":" + password;
            String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encoding);
        }

        connection.setDoInput(true);
        connection.setRequestProperty("User-Agent", "Katalyzer");
        return connection;
    }
}
