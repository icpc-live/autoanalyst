package jsonfeed;

import io.HttpFeedClient;
import model.Contest;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;

public class EventParsingTests {
    private static final Logger log = LogManager.getLogger(EventParsingTests.class);


    public Reader resourceReader(String resourceName) {
        InputStream input = this.getClass().getResourceAsStream(resourceName);
        return new InputStreamReader(input);
    }


    @Test
    public void simpleJsonParsing() throws IOException {
        JsonEventReader reader = new JsonEventReader();
        ArrayList<JsonEvent> events = reader.parse(resourceReader("/feed-ukiepc2017.ndjson"));

        Assert.assertEquals(31692,events.size());
    }

    @Test
    public void setupAndParseSomeTeams() throws Exception {
        JsonEventReader reader = new JsonEventReader();
        ArrayList<JsonEvent> events = reader.parse(resourceReader("/feed-ukiepc2017.ndjson"));

        StandardEventHandlers handlers = new StandardEventHandlers();

        Contest testContest = new Contest();

        for (JsonEvent event : events) {
            JsonEventHandler handler = handlers.getHandlerFor(event);
            if (handler == null) {
                Assert.fail(String.format("Event %s had no defined handler", event));
            }
            handler.process(testContest, event);
        }
    }

    @Test
    public void setupAndParseNwercTeams() throws Exception {
        JsonEventReader reader = new JsonEventReader();
        ArrayList<JsonEvent> events = reader.parse(resourceReader("/feed-nwerc2017-c.json"));

        StandardEventHandlers handlers = new StandardEventHandlers();

        Contest testContest = new Contest();

        for (JsonEvent event : events) {
            JsonEventHandler handler = handlers.getHandlerFor(event);
            if (handler == null) {
                Assert.fail(String.format("Event %s had no defined handler", event));
            }
            try {
                handler.process(testContest, event);
            }
            catch (Exception e) {
                log.error(String.format("Error processing event %s : %s", event, e));
            }
        }
    }

    @Test
    public void testContestTimeParsing() {

        long result = new TimeConverter().parseContestTimeMillis("0:00:46.331");
        Assert.assertEquals(46331, result);

    }



}
