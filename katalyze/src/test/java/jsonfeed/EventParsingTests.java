package jsonfeed;

import model.Contest;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;

public class EventParsingTests {
    private static final Logger log = LogManager.getLogger(EventParsingTests.class);


    public Reader resourceReader(String resourceName) {
        InputStream input = this.getClass().getResourceAsStream(resourceName);
        assert input != null;
        return new InputStreamReader(input);
    }


    @Test
    public void simpleJsonParsing() throws IOException {
        JsonEventReader reader = new JsonEventReader();
        ArrayList<JsonEvent> events = reader.parse(resourceReader("/feed-ukiepc2017.ndjson"));

        Assertions.assertEquals(31693,events.size());
    }

    @Test
    public void setupAndParseSomeTeams() throws Exception {
        JsonEventReader reader = new JsonEventReader();
        ArrayList<JsonEvent> events = reader.parse(resourceReader("/feed-ukiepc2017.ndjson"));

        StandardEventHandlers handlers = new StandardEventHandlers();

        Contest testContest = new Contest();

        for (JsonEvent event : events) {
            if (event == null) {
                continue;
            }
            JsonEventHandler handler = handlers.getHandlerFor(event);
            if (handler == null) {
                Assertions.fail(String.format("Event %s had no defined handler", event));
            }
            handler.process(testContest, event);
        }
    }

    @Test
    public void testContestTimeParsing() {

        long result = new TimeConverter().parseContestTimeMillis("0:00:46.331");
        Assertions.assertEquals(46331, result, "with milliseconds");

        long result2 = new TimeConverter().parseContestTimeMillis("0:00:46");
        Assertions.assertEquals(46000, result2, "without milliseconds");


    }

    @Test
    public void testIsoTimeParsing() {
        long result = new TimeConverter().parseTimestampMillis("2018-03-07T17:00:00.000+08:00");
        Assertions.assertEquals(1520413200000L, result);

        long result2 = new TimeConverter().parseTimestampMillis("2018-03-07T17:00:00.000+08");
        Assertions.assertEquals(1520413200000L, result2);


    }

    @Test
    public void testContestTimeGenerating() {
        String result = new TimeConverter().toContestTime(2*60000+46331);
        Assertions.assertEquals("0:02:46.331", result);
    }



}
