package model;

import jsonfeed.TimeConverter;
import net.sf.json.JSONObject;
import java.time.Instant;
import java.util.Comparator;
import java.util.function.Predicate;

public class ContestProperties implements ApiEntity {
    private final static TimeConverter converter = new TimeConverter();

    private String id;
    private String name;
    private String formalName;
    private long startTimeMillis;
    private long durationMillis;
    private long scoreboardFreezeMillis;
    private int penaltyTime;

    public static ContestProperties fromJSON(JSONObject src) {
        ContestProperties target = new ContestProperties();
        target.id = src.getString("id");
        target.name = src.getString("name");
        target.formalName = src.optString("formal_name", null);

        String startTime = src.optString("start_time", null);
        if (startTime != null && !startTime.equals("null")) {
            target.startTimeMillis = converter.parseTimestampMillis(startTime);
        } else {
	    String countdownPauseTime = src.optString("countdown_pause_time", null);
	    if (countdownPauseTime != null && !countdownPauseTime.equals("null")) {
		target.startTimeMillis = converter.parseContestTimeMillis(countdownPauseTime) + Instant.now().toEpochMilli();
	    }
	}
        target.durationMillis = converter.parseContestTimeMillis(src.getString("duration"));
        target.scoreboardFreezeMillis = converter.parseContestTimeMillis(src.getString("scoreboard_freeze_duration"));
        target.penaltyTime = src.getInt("penalty_time");
        return target;
    }

    private ContestProperties() {}

    public ContestProperties(String name, int penaltyTime, long scoreboardFreezeMillis) {
        this.name = name;
        this.formalName = name;
        this.penaltyTime = penaltyTime;
        this.scoreboardFreezeMillis = scoreboardFreezeMillis;
    }

    public int getPenaltyTime() {
        return penaltyTime;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getStartTimeEpochSeconds() {
        return (startTimeMillis / 1000);
    }

    public long getEndTimeMillis() {
        return startTimeMillis+durationMillis;
    }

    public long getDurationMillis() { return durationMillis; }

    public long getScoreboardFreezeMillis() {return scoreboardFreezeMillis; }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }


}
