package jsonfeed;

import model.Contest;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.stream.Stream;

public class EventParsingTests {


    public InputStream resourceAsSteam(String resourceName) {
        InputStream input = this.getClass().getResourceAsStream(resourceName);
        return input;
    }


    @Test
    public void simpleJsonParsing() {
        JsonEventReader reader = new JsonEventReader();
        ArrayList<JsonEvent> events = reader.parse(resourceAsSteam("/feed-ukiepc2017.ndjson"));

        Assert.assertEquals(31692,events.size());
    }

    @Test
    public void setupAndParseSomeTeams() throws Exception {
        JsonEventReader reader = new JsonEventReader();
        ArrayList<JsonEvent> events = reader.parse(resourceAsSteam("/feed-ukiepc2017.ndjson"));

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


}
