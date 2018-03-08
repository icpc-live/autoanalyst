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
    private String externalId;
    private String shortName;
    private long endTimeMillis;

    public static ContestProperties fromJSON(JSONObject src) {
        ContestProperties target = new ContestProperties();
        target.id = src.getString("id");
        target.name = src.getString("name");
        target.formalName = src.getString("formal_name");
        target.startTimeMillis = converter.parseTimestampMillis(src.getString("start_time"));
        target.durationMillis = converter.parseContestTimeMillis(src.getString("duration"));
        target.endTimeMillis = converter.parseTimestampMillis(src.getString("end_time"));
        target.penaltyTime = src.getInt("penalty_time");
        target.shortName = src.getString("shortname");
        target.externalId = src.getString("external_id");
        return target;
    }

    public long getPenaltyTime() {
        return penaltyTime;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }


}
