package model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jsonfeed.TimeConverter;
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
    private boolean countdownIsPaused = false;

    public static ContestProperties fromJSON(JsonObject src) {
        ContestProperties target = new ContestProperties();
        target.id = src.getAsJsonPrimitive("id").getAsString();
        target.name = src.getAsJsonPrimitive("name").getAsString();
        target.formalName = JsonHelpers.optString(src,"formal_name");

        String startTime = JsonHelpers.optString(src, "start_time");
        if (startTime != null && !startTime.equals("null")) {
            target.startTimeMillis = converter.parseTimestampMillis(startTime);
        } else {
            String countdownPauseTime = JsonHelpers.optString(src,"countdown_pause_time");
            if (countdownPauseTime != null && !countdownPauseTime.equals("null")) {
                target.countdownIsPaused = true;
                target.startTimeMillis = converter.parseContestTimeMillis(countdownPauseTime)
                        + Instant.now().toEpochMilli();
            }
        }
        target.durationMillis = converter.parseContestTimeMillis(src.getAsJsonPrimitive("duration").getAsString());
        target.scoreboardFreezeMillis = converter.parseContestTimeMillis(src.getAsJsonPrimitive("scoreboard_freeze_duration").getAsString());
        target.penaltyTime = src.getAsJsonPrimitive("penalty_time").getAsInt();
        return target;
    }

    private ContestProperties() {
    }

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
        return startTimeMillis + durationMillis;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public long getScoreboardFreezeMillis() {
        return scoreboardFreezeMillis;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isCountdownPaused() {
        return countdownIsPaused;
    }

}
