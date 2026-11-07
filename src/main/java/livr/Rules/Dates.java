package livr.Rules;

import livr.FunctionKeeper;
import livr.LIVRUtils;
import livr.RuleArguments;
import org.json.simple.JSONObject;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * iso_date, iso_datetime and iso_time with their "min" and "max" bounds (LIVR 2.5, 3.3).
 * "today", "yesterday" and "tomorrow" are dates in UTC from the clock of the validator,
 * see Validator.setClock
 */
public class Dates {
    private static final Pattern DATE = Pattern.compile("([0-9]{4})-([0-9]{2})-([0-9]{2})");
    private static final Pattern DATETIME = Pattern.compile(
            "([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2})(?::([0-9]{2})(?:\\.([0-9]{1,9}))?)?(?:Z|([+-])([0-9]{2}):([0-9]{2}))");
    private static final Pattern TIME = Pattern.compile("([0-9]{2}):([0-9]{2})(?::([0-9]{2})(?:\\.([0-9]{1,9}))?)?");

    private static final long DAY_MILLIS = 86400000L;

    /**
     * A date YYYY-MM-DD; {"iso_date": {"min": ..., "max": ...}} with dates or "today",
     * "yesterday", "tomorrow" as bounds. The output is the value unchanged
     */
    public static Function<List<Object>, Function> iso_date = objects -> {
        Map<?, ?> options = options(objects, "iso_date");
        Bound min = dateBound(options, "min");
        Bound max = dateBound(options, "max");
        checkOrder(min, max, "iso_date");
        Clock clock = RuleArguments.clock(objects);

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            Object value = wrapper.getValue();
            if (LIVRUtils.isEmpty(value)) return "";
            if (!LIVRUtils.isPrimitive(value)) return "FORMAT_ERROR";
            if (!(value instanceof String)) return "WRONG_DATE";

            LocalDate date = parseDate((String) value);
            if (date == null) return "WRONG_DATE";

            if (min != null && date.toEpochDay() < min.epochDay(clock)) return "DATE_TOO_EARLY";
            if (max != null && date.toEpochDay() > max.epochDay(clock)) return "DATE_TOO_LATE";
            return "";
        };
    };

    /**
     * A date and a time with an offset; the output is the instant in UTC as
     * YYYY-MM-DDTHH:MM:SS.sssZ. A date bound is the start of the day as "min", its end as "max"
     */
    public static Function<List<Object>, Function> iso_datetime = objects -> {
        Map<?, ?> options = options(objects, "iso_datetime");
        Bound min = datetimeBound(options, "min");
        Bound max = datetimeBound(options, "max");
        checkOrder(min, max, "iso_datetime");
        Clock clock = RuleArguments.clock(objects);

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            Object value = wrapper.getValue();
            if (LIVRUtils.isEmpty(value)) return "";
            if (!LIVRUtils.isPrimitive(value)) return "FORMAT_ERROR";
            if (!(value instanceof String)) return "WRONG_DATETIME";

            Long millis = parseDatetime((String) value);
            if (millis == null) return "WRONG_DATETIME";

            // It must be written as YYYY-MM-DDTHH:MM:SS.sssZ
            LocalDateTime utc = LocalDateTime.ofEpochSecond(Math.floorDiv(millis, 1000L),
                    (int) Math.floorMod(millis, 1000L) * 1000000, ZoneOffset.UTC);
            if (utc.getYear() < 0 || utc.getYear() > 9999) return "WRONG_DATETIME";

            if (min != null && millis < min.millis(clock, false)) return "DATETIME_TOO_EARLY";
            if (max != null && millis > max.millis(clock, true)) return "DATETIME_TOO_LATE";

            wrapper.getFieldResultArr().add(String.format(Locale.ROOT, "%04d-%02d-%02dT%02d:%02d:%02d.%03dZ",
                    utc.getYear(), utc.getMonthValue(), utc.getDayOfMonth(),
                    utc.getHour(), utc.getMinute(), utc.getSecond(), utc.getNano() / 1000000));
            return "";
        };
    };

    /**
     * A time of day HH:MM, HH:MM:SS or HH:MM:SS.fraction without an offset. min > max is a range
     * over midnight. The output is the value unchanged
     */
    public static Function<List<Object>, Function> iso_time = objects -> {
        Map<?, ?> options = options(objects, "iso_time");
        Long min = timeBound(options, "min");
        Long max = timeBound(options, "max");

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            Object value = wrapper.getValue();
            if (LIVRUtils.isEmpty(value)) return "";
            if (!LIVRUtils.isPrimitive(value)) return "FORMAT_ERROR";
            if (!(value instanceof String)) return "WRONG_TIME";

            Long time = parseTime((String) value);
            if (time == null) return "WRONG_TIME";

            if (min != null && max != null && min > max) {
                // Over midnight: a time in the gap gets the error of the nearer bound
                if (time >= min || time <= max) return "";
                return time - max < min - time ? "TIME_TOO_LATE" : "TIME_TOO_EARLY";
            }
            if (min != null && time < min) return "TIME_TOO_EARLY";
            if (max != null && time > max) return "TIME_TOO_LATE";
            return "";
        };
    };

    /**
     * A date, a datetime (in milliseconds) or "today", "yesterday", "tomorrow" (days from today)
     */
    private static class Bound {
        final boolean isRelative;
        final long value;
        final boolean isDate;

        Bound(boolean isRelative, long value, boolean isDate) {
            this.isRelative = isRelative;
            this.value = value;
            this.isDate = isDate;
        }

        long epochDay(Clock clock) {
            return isRelative ? today(clock) + value : value;
        }

        /**
         * A date bound is the start of the day as "min" and its last millisecond as "max"
         */
        long millis(Clock clock, boolean isMax) {
            if (!isDate) return value;
            return epochDay(clock) * DAY_MILLIS + (isMax ? DAY_MILLIS - 1 : 0);
        }

        long order(boolean isMax) {
            return isDate ? value * DAY_MILLIS + (isMax ? DAY_MILLIS - 1 : 0) : value;
        }
    }

    private static long today(Clock clock) {
        return clock.instant().atOffset(ZoneOffset.UTC).toLocalDate().toEpochDay();
    }

    /**
     * The options object, the first argument if there is one; the arguments after it are ignored.
     * {"iso_date": null} is the plain iso_date: LIVR 2.0 ignored the arguments of iso_date
     * (LIVR 2.5, 2.6). For the other rules null is a wrong argument
     */
    private static Map<?, ?> options(List<Object> objects, String ruleName) {
        List<Object> args = RuleArguments.get(objects);
        boolean isNullForIsoDate = ruleName.equals("iso_date") && args.size() == 1 && args.get(0) == null;
        if (args.isEmpty() || isNullForIsoDate) return new JSONObject();
        if (!(args.get(0) instanceof JSONObject)) {
            throw RuleArguments.schemaError("the argument of \"" + ruleName + "\" must be an options object");
        }

        Map<?, ?> options = (Map<?, ?>) args.get(0);
        for (Object key : options.keySet()) {
            if (!key.equals("min") && !key.equals("max")) {
                throw RuleArguments.schemaError("unknown option \"" + key + "\" of \"" + ruleName + "\"");
            }
        }
        return options;
    }

    private static Bound relativeBound(Object bound) {
        if ("today".equals(bound)) return new Bound(true, 0, true);
        if ("yesterday".equals(bound)) return new Bound(true, -1, true);
        if ("tomorrow".equals(bound)) return new Bound(true, 1, true);
        return null;
    }

    private static Bound dateBound(Map<?, ?> options, String name) {
        if (!options.containsKey(name)) return null;
        Object bound = options.get(name);

        Bound relative = relativeBound(bound);
        if (relative != null) return relative;

        LocalDate date = bound instanceof String ? parseDate((String) bound) : null;
        if (date == null) throw RuleArguments.schemaError("\"" + name + "\" of \"iso_date\" must be a date, \"today\", \"yesterday\" or \"tomorrow\"");
        return new Bound(false, date.toEpochDay(), true);
    }

    private static Bound datetimeBound(Map<?, ?> options, String name) {
        if (!options.containsKey(name)) return null;
        Object bound = options.get(name);

        Bound relative = relativeBound(bound);
        if (relative != null) return relative;

        if (bound instanceof String) {
            LocalDate date = parseDate((String) bound);
            if (date != null) return new Bound(false, date.toEpochDay(), true);

            Long millis = parseDatetime((String) bound);
            if (millis != null) return new Bound(false, millis, false);
        }
        throw RuleArguments.schemaError("\"" + name + "\" of \"iso_datetime\" must be a datetime, a date, \"today\", \"yesterday\" or \"tomorrow\"");
    }

    private static Long timeBound(Map<?, ?> options, String name) {
        if (!options.containsKey(name)) return null;
        Object bound = options.get(name);

        Long time = bound instanceof String ? parseTime((String) bound) : null;
        if (time == null) throw RuleArguments.schemaError("\"" + name + "\" of \"iso_time\" must be a time");
        return time;
    }

    /**
     * min later than max is a schema error when both bounds are fixed or both are relative
     */
    private static void checkOrder(Bound min, Bound max, String ruleName) {
        if (min == null || max == null || min.isRelative != max.isRelative) return;
        if (min.order(false) > max.order(true)) {
            throw RuleArguments.schemaError("\"min\" of \"" + ruleName + "\" is later than \"max\"");
        }
    }

    /**
     * A valid calendar date YYYY-MM-DD, or null
     */
    private static LocalDate parseDate(String value) {
        Matcher matcher = DATE.matcher(value);
        if (!matcher.matches()) return null;
        return date(matcher.group(1), matcher.group(2), matcher.group(3));
    }

    private static LocalDate date(String year, String month, String day) {
        try {
            return LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        } catch (DateTimeException e) {
            return null;
        }
    }

    /**
     * Milliseconds since the epoch of a datetime with an offset, or null. Further fraction digits
     * are cut off. The offset may be up to 23:59, more than ZoneOffset allows
     */
    private static Long parseDatetime(String value) {
        Matcher matcher = DATETIME.matcher(value);
        if (!matcher.matches()) return null;

        LocalDate date = date(matcher.group(1), matcher.group(2), matcher.group(3));
        int hour = Integer.parseInt(matcher.group(4));
        int minute = Integer.parseInt(matcher.group(5));
        int second = matcher.group(6) == null ? 0 : Integer.parseInt(matcher.group(6));
        String fraction = matcher.group(7) == null ? "" : matcher.group(7);
        int millis = Integer.parseInt((fraction + "000").substring(0, 3));
        if (date == null || hour > 23 || minute > 59 || second > 59) return null;

        long offset = 0;
        if (matcher.group(8) != null) {
            int offsetHour = Integer.parseInt(matcher.group(9));
            int offsetMinute = Integer.parseInt(matcher.group(10));
            if (offsetHour > 23 || offsetMinute > 59) return null;
            offset = (matcher.group(8).equals("-") ? -1 : 1) * (offsetHour * 3600L + offsetMinute * 60L);
        }

        long seconds = date.toEpochDay() * 86400L + hour * 3600L + minute * 60L + second - offset;
        return seconds * 1000L + millis;
    }

    /**
     * Nanoseconds since midnight of a time HH:MM[:SS[.fraction]], or null
     */
    private static Long parseTime(String value) {
        Matcher matcher = TIME.matcher(value);
        if (!matcher.matches()) return null;

        int hour = Integer.parseInt(matcher.group(1));
        int minute = Integer.parseInt(matcher.group(2));
        int second = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
        String fraction = matcher.group(4) == null ? "" : matcher.group(4);
        if (hour > 23 || minute > 59 || second > 59) return null;

        long nanos = Long.parseLong((fraction + "000000000").substring(0, 9));
        return ((hour * 60L + minute) * 60L + second) * 1000000000L + nanos;
    }
}
