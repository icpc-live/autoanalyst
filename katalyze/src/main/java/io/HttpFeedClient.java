package io;

import jsonfeed.ContestEntry;
import jsonfeed.DummyTrustManager;
import model.ContestProperties;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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

    public JSON getJson(String urlString) throws IOException {
        InputStream is = getInputStream(urlString);

        String jsonString = IOUtils.toString(is, "UTF-8");

        JSON json = (JSON) JSONSerializer.toJSON( jsonString );
        return json;
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

        JSON response = getJson(baseUrl);
        if (response.isArray()) {
            // We got an array of contests;
            JSONArray responseArray = (JSONArray) response;
            responseArray.forEach(src -> {
                if (src instanceof JSONObject) {
                    ContestProperties props = ContestProperties.fromJSON((JSONObject) src);
                    ContestEntry newEntry = new ContestEntry(props, baseUrl+"/"+props.getId());
                    target.add(newEntry);
                }
            });
        } else {
            // Url pointed directly at one particular contest
            ContestProperties props = ContestProperties.fromJSON((JSONObject) response);
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
