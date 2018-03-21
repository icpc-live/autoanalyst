package web;

import jsonfeed.TimeConverter;
import model.LoggableEvent;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.io.Writer;

public class LoggableEventSerializer implements EventSerializer<LoggableEvent> {
    TimeConverter timeConverter = new TimeConverter();


    private JSONObject getJsonObject(LoggableEvent event) {
        JSONObject innerInfo = new JSONObject()
                .element("id", Integer.toString(event.id));

        if (event.team != null) {
            innerInfo = innerInfo.element("team_id", event.team.getId());
        }

        if (event.problem != null) {
            innerInfo = innerInfo.element("problem_id", event.problem.getId());
        }

        if (event.submission != null) {
            innerInfo = innerInfo.element("submission_id", event.submission.getId());
        }

		innerInfo = innerInfo
                .element("priority", event.importance.ordinal())
                .element("text", event.message)
                .element("contest_time", timeConverter.toContestTime(event.contestTimeMillis));


        JSONObject feedEntry = new JSONObject()
                .element("id", Integer.toString(event.id))
                .element("type", "commentary-messages")
                .element("op", "create")
                .element("data", innerInfo);

        return feedEntry;
    }


    public void write(LoggableEvent data, Writer output) throws IOException {
        JSONObject json = getJsonObject(data);
        json.write(output);
        output.write("\n");
    }
}
