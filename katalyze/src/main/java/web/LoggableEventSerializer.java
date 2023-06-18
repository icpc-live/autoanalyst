package web;

import jsonfeed.TimeConverter;
import model.LoggableEvent;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.io.Writer;

public class LoggableEventSerializer implements EventSerializer<LoggableEvent> {
    TimeConverter timeConverter = new TimeConverter();


    private JSONObject getJsonObject(LoggableEvent event) {
        JSONObject innerInfo = new JSONObject()
                .element("id", Integer.toString(event.id));

        JSONArray team_ids = new JSONArray();
        if (event.team != null) {
            team_ids.add(event.team.getId());
        }
        innerInfo = innerInfo.element("team_ids", team_ids);

        JSONArray problem_ids = new JSONArray();
        if (event.problem != null) {
		problem_ids.add(event.problem.getId());
        }
        innerInfo = innerInfo.element("problem_ids", problem_ids);

        JSONArray submission_ids = new JSONArray();
        if (event.submission != null) {
		submission_ids.add(event.submission.getId());
        }
        innerInfo = innerInfo.element("submission_ids", submission_ids);

		innerInfo = innerInfo
				.element("id", Integer.toString(event.id))
                .element("priority", event.importance.ordinal())
                .element("message", event.message)
                .element("contest_time", timeConverter.toContestTime(event.contestTimeMillis))
                .element("time", event.timestamp.toString());


        JSONObject feedEntry = new JSONObject()
                .element("id", Integer.toString(event.id))
                .element("type", "commentary")
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
