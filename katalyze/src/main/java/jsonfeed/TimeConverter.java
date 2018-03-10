package jsonfeed;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class TimeConverter {
    private final SimpleDateFormat timespanFormatWithMillis;
    private final SimpleDateFormat timespanFormatWithoutMillis;

    private final TimeZone utc;

    public TimeConverter() {

        utc = TimeZone.getTimeZone("UTC");
        timespanFormatWithMillis = new SimpleDateFormat("H:mm:ss.SSS");
        timespanFormatWithMillis.setTimeZone(utc);

        timespanFormatWithoutMillis = new SimpleDateFormat("H:mm:ss");
        timespanFormatWithoutMillis.setTimeZone(utc);

    }

    public long parseTimestampMillis(String timeString) {

        // Awful hack to get around Java's lack of full ISO-8601 support
        int plusIndex = timeString.indexOf("+");
        if (plusIndex >= timeString.length()-3) {
            timeString = timeString + ":00";
        }

        ZonedDateTime instant = ZonedDateTime.parse(timeString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        long millis = instant.toEpochSecond()*1000+(instant.getNano()/1000000);
        return millis;
    }


    public long parseContestTimeMillis(String timeString) {
        try {
            if (timeString == null) {
                return -1;
            }
            if (timeString.contains(".")) {
                Date result = timespanFormatWithMillis.parse(timeString);
                return result.toInstant().toEpochMilli();
            } else {
                Date result = timespanFormatWithoutMillis.parse(timeString);
                return result.toInstant().toEpochMilli();
            }
        } catch (ParseException e) {
            return -1;
        }
    }

    public String toContestTime(long millis) {
        Date input = Date.from(Instant.ofEpochMilli(millis));
        return timespanFormatWithMillis.format(input);
    }
}
