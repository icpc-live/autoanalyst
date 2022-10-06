package io;

import jsonfeed.ContestEntry;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

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



    private Predicate<ContestEntry> filterByPrefix(String prefix) {
        if (prefix == null) {
            return x -> true;
        } else {
            String lowercasePrefix = prefix.toLowerCase();
            return x -> x.getLowercaseId().startsWith(lowercasePrefix);
        }
    }


    public InputStreamProvider createHttpReader() throws IOException {
        String contestIdPrefix = config.getString("CDS.contestIdPrefix");

        HttpFeedClient feedClient = createFeedClient();


        ContestEntry entry = feedClient.guessContest(getApiBase(), filterByPrefix(contestIdPrefix));
        if (entry == null) {
            log.error(String.format("Unable to locate a contest whose feed to read at %s", getApiBase()));
            return null;
        } else {
            log.info(String.format("Will read from contest %s", entry));
        }

        return (resumePoint, isStreamToken) -> {
            String url = entry.url+"/event-feed";
            String argumentName = (isStreamToken) ? "since_token" : "since_id";

            String resumeQuery = (resumePoint != null) ? "?"+argumentName+"="+URLEncoder.encode(resumePoint, StandardCharsets.UTF_8.name()) : "";
            return feedClient.getInputStream(url+resumeQuery);
        };
    }


    public InputStreamProvider createFileReader(String file) throws FileNotFoundException {
        FileInputStream inputFile = new FileInputStream(file);

        return (resumePoint, isStreamToken) -> inputFile;
    }

    public InputStreamProvider createConsoleReader() {
        return (resumePoint, isStreamToken) -> System.in;
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
