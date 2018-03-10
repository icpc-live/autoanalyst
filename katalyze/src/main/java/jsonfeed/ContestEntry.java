package jsonfeed;

import model.ContestProperties;

import java.util.Comparator;
import java.util.function.Predicate;

public class ContestEntry {
    public final ContestProperties contest;
    public final String url;
    public final String lowercaseId;


    public static final Predicate<ContestEntry> notInThePast(long currentTime) {
        return x -> x.contest.getEndTimeMillis() > currentTime;
    }

    public boolean isActive(long now) {
        return (contest.getStartTimeMillis() <= now && contest.getEndTimeMillis() > now);
    }

    public boolean isActiveWithinAnHour(long now) {
        return (contest.getStartTimeMillis()-(3600000) <= now && contest.getEndTimeMillis() > now);
    }

    public long getStartTime() {
        return contest.getStartTimeMillis();
    }

    public long getEndTime() {
        return contest.getEndTimeMillis();
    }

    public String getLowercaseId() { return lowercaseId; }

    public ContestEntry(ContestProperties contest, String url) {
        this.contest = contest;
        this.lowercaseId = contest.getId().toLowerCase();
        this.url = url;
    }

    public String toString() {
        return String.format("[%s] (%s)", contest.getId(), url);
    }

}
