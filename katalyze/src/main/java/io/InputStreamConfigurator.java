package io;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

public class InputStreamConfigurator {
    Configuration config;

    public InputStreamConfigurator(Configuration config) {
        this.config = config;
    }

    public InputStreamProvider createHttpReader() throws MalformedURLException {
        String apiBase = config.getString("CDS.baseurl");
        String feedUrl = String.format("%s/event-feed", apiBase);

        String user = config.getString("CDS.user");
        String pass = config.getString("CDS.pass");

        boolean bypassCert = config.getBoolean("CDS.bypasscert", false);

        HttpFeedClient client = new HttpFeedClient(feedUrl);
        if (bypassCert) {
            client.bypassCertErrors_DANGER(true);
        }

        client.setCredentials(user, pass);

        return client;
    }

    public InputStreamProvider createFileReader(String file) throws FileNotFoundException {
        FileInputStream inputFile = new FileInputStream(file);

        return () -> inputFile;
    }

    public InputStreamProvider createConsoleReader() {
        return () -> System.in;
    }

    public InputStreamProvider getInputFromConfig() throws ConfigurationException  {
        try {
            String source = config.getString("source", "stdin").toLowerCase();
            if (source.equals("cds")) {
                return createHttpReader();
            }
            if (source.equals("stdin")) {
                return createConsoleReader();
            } else {
                throw new ConfigurationException(String.format("%s is not a recognized data source", source));
            }
        }
        catch (MalformedURLException e) {
            throw new ConfigurationException(String.format("The given url was not in a valid format. Error %s", e));
        }

    }
}
