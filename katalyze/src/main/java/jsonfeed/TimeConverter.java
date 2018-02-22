package jsonfeed;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

public class TimeConverter {
    private final SimpleDateFormat timespanFormat;
    private final TimeZone utc;

    public TimeConverter() {

        utc = TimeZone.getTimeZone("UTC");
        timespanFormat = new SimpleDateFormat("H:mm:ss.SSS");
        timespanFormat.setTimeZone(utc);
    }

    public long parseContestTimeMillis(String timeString) {
        try {
            if (timeString == null) {
                return -1;
            }
            Date result = timespanFormat.parse(timeString);
            return result.toInstant().toEpochMilli();
        } catch (ParseException e) {
            return -1;
        }
    }

    public String toContestTime(long millis) {
        Date input = Date.from(Instant.ofEpochMilli(millis));
        return timespanFormat.format(input);
    }
}
