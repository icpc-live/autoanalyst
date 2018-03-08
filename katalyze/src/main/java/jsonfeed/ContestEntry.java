package jsonfeed;

import model.ContestProperties;

import java.util.Comparator;
import java.util.function.Predicate;

public class ContestEntry {
    public final ContestProperties contest;
    public final String url;


    public static final Predicate<ContestEntry> notInThePast(long currentTime) {
        return x -> x.contest.getEndTimeMillis() > currentTime;
    }

    public boolean isActive(long now) {
        return (contest.getStartTimeMillis() <= now && contest.getEndTimeMillis() > now);
    }

    public long getStartTime() {
        return contest.getStartTimeMillis();
    }



    public ContestEntry(ContestProperties contest, String url) {
        this.contest = contest;
        this.url = url;
    }

    public String toString() {
        return String.format("[%s] (%s)", contest.getId(), url);
    }

}
