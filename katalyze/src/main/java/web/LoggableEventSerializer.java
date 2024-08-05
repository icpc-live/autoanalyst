package web;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jsonfeed.TimeConverter;
import model.LoggableEvent;

import java.io.IOException;
import java.io.Writer;

public class LoggableEventSerializer implements EventSerializer<LoggableEvent> {
    TimeConverter timeConverter = new TimeConverter();


    private JsonObject getJsonObject(LoggableEvent event) {
        JsonObject innerInfo = new JsonObject();
        innerInfo.addProperty("id", Integer.toString(event.id));

        JsonArray team_ids = new JsonArray();
        if (event.team != null) {
            team_ids.add(event.team.getId());
        }
        innerInfo.add("team_ids", team_ids);

        JsonArray problem_ids = new JsonArray();
        if (event.problem != null) {
		    problem_ids.add(event.problem.getId());
        }
        innerInfo.add("problem_ids", problem_ids);

        JsonArray submission_ids = new JsonArray();
        if (event.submission != null) {
		    submission_ids.add(event.submission.getId());
        }
        innerInfo.add("submission_ids", submission_ids);

		innerInfo.addProperty("id", Integer.toString(event.id));
        innerInfo.addProperty("priority", event.importance.ordinal());
        innerInfo.addProperty("message", event.message);
        innerInfo.addProperty("contest_time", timeConverter.toContestTime(event.contestTimeMillis));
        innerInfo.addProperty("time", event.timestamp.toString());


        JsonObject feedEntry = new JsonObject();
        feedEntry.addProperty("id", Integer.toString(event.id));
        feedEntry.addProperty("type", "commentary");
        feedEntry.addProperty("op", "create");
        feedEntry.add("data", innerInfo);

        return feedEntry;
    }


    public void write(LoggableEvent data, Writer output) throws IOException {
        JsonObject json = getJsonObject(data);
        new Gson().toJson(json, output);
        output.write("\n");
    }
}
