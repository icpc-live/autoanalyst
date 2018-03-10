package model;

import jsonfeed.TimeConverter;
import net.sf.json.JSONObject;

import java.util.Comparator;
import java.util.function.Predicate;

public class ContestProperties {
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
        target.formalName = src.getString("formal_name");
        target.startTimeMillis = converter.parseTimestampMillis(src.getString("start_time"));
        target.durationMillis = converter.parseContestTimeMillis(src.getString("duration"));
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

    public long getEndTimeMillis() {
        return startTimeMillis+durationMillis;
    }

    public long getDurationMillis() { return durationMillis; }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }


}
