package io;

import jsonfeed.DummyTrustManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;

public class HttpFeedClient implements InputStreamProvider {
    private static Logger log = LogManager.getLogger(HttpFeedClient.class);


    private String user = null;
    private String password = null;
    private boolean bypassCertificateErrors = false;
    private final URL url;
    private HttpURLConnection connection;


    public HttpFeedClient(String url) throws MalformedURLException{
        this.url = new URL(url);
    }

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


    public InputStream getInputStream() throws IOException {
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


        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);

        if (bypassCertificateErrors && connection instanceof HttpsURLConnection) {
            HttpsURLConnection tlsConnection = (HttpsURLConnection) connection;
            tlsConnection.setHostnameVerifier((s, session) -> true);
        }

        if (user != null && password != null) {
            String userPassword = user + ":" + password;
            String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encoding);
        }

        connection.setDoInput(true);
        connection.setRequestProperty("User-Agent", "Katalyzer");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Got unexpected response code: "+Integer.toString(200));
        }

        InputStream is = connection.getInputStream();
        return is;
    }
}
