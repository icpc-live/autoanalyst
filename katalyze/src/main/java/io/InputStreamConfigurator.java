package io;

import jsonfeed.ContestEntry;
import model.ContestProperties;
import net.sf.json.JSON;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

public class InputStreamConfigurator {
    private static Logger log = LogManager.getLogger(InputStreamConfigurator.class);

    Configuration config;

    public InputStreamConfigurator(Configuration config) {
        this.config = config;
    }

    public HttpFeedClient createFeedClient() {

        String user = config.getString("CDS.user");
        String pass = config.getString("CDS.pass");

        boolean bypassCert = config.getBoolean("CDS.bypasscert", false);

        HttpFeedClient client = new HttpFeedClient();
        if (bypassCert) {
            client.bypassCertErrors_DANGER(true);
        }

        client.setCredentials(user, pass);

        return client;
    }


    public String getApiBase() {
        return config.getString("CDS.baseurl");
    }



    public InputStreamProvider createHttpReader() throws IOException {
        HttpFeedClient feedClient = createFeedClient();

        ContestEntry entry = feedClient.guessContest(getApiBase());
        if (entry == null) {
            log.error(String.format("Unable to locate a contest whose feed to read at %s", getApiBase()));
            return null;
        } else {
            log.info(String.format("Will read from contest %s", entry));
        }



        return () -> feedClient.getInputStream(entry.url+"/event-feed");
    }



    public InputStreamProvider createFileReader(String file) throws FileNotFoundException {
        FileInputStream inputFile = new FileInputStream(file);

        return () -> inputFile;
    }

    public InputStreamProvider createConsoleReader() {
        return () -> System.in;
    }

    public InputStreamProvider getInputFromConfig() throws ConfigurationException, IOException  {
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
