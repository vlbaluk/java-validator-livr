package livr.Rules;

import livr.FunctionKeeper;
import livr.LIVRUtils;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Created by vladislavbaluk on 9/28/2017.
 */
public class Special {

    public static Function<List<Object>, Function> email = objects -> {
        final Pattern VALID_EMAIL_ADDRESS_REGEX =
                Pattern.compile("^(?:[^<>()\\[\\]\\\\.,;:\\p{IsWhite_Space}@\"]++(?:\\.[^<>()\\[\\]\\\\.,;:\\p{IsWhite_Space}@\"]++)*+|\"[^@\\n\\r\\u2028\\u2029]+\")@(?:\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]|(?:[a-zA-Z\\-0-9]++\\.)++[a-zA-Z]{2,})$");
        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";

            String value = wrapper.getValue() + "";

            if (!VALID_EMAIL_ADDRESS_REGEX.matcher(value).matches()) return "WRONG_EMAIL";

            wrapper.getFieldResultArr().add(value);
            return "";
        };
    };

    public static Function<List<Object>, Function> equal_to_field = objects -> {
        final String field = objects.get(0) + "";
        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";
            String value = wrapper.getValue() + "";
            Object other = wrapper.getArgs().get(field);

            if (other == null || !value.equals(other + "")) return "FIELDS_NOT_EQUAL";
            return "";
        };
    };

    // Letters of host labels: Latin letters and U+00A1-U+FFFF, so Unicode hosts pass
    private static final String URL_LETTER = "a-z\\u00a1-\\uffff";
    // 1-63 characters, not starting or ending with "-". "." is not in a label, so labels do not overlap
    private static final String URL_LABEL = "[" + URL_LETTER + "0-9](?:[" + URL_LETTER + "0-9-]{0,61}[" + URL_LETTER + "0-9])?";
    // The first byte is 1-223, the last one is not 255
    private static final String URL_IPV4 = "(?:[1-9]\\d?|1\\d\\d|2[0-1]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[0-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))";
    // 0-65535 in 1-5 digits
    private static final String URL_PORT = "(?:\\d{1,4}|[0-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])";

    /**
     * Optional user info (any characters but white space, ":" included, before "@"), then an IPv4
     * address, labels with a top-level domain of 2-63 letters or localhost, no dot after the host
     * (LIVR 2.5), an optional port, a path, query or fragment. Every label ends with its ".", and
     * the top-level domain has none, so the labels are matched once (++)
     */
    public static Function<List<Object>, Function> url = objects -> {
        Pattern pattern = Pattern.compile("^(?:https?://)(?:\\P{IsWhite_Space}+@)?"
                + "(?:" + URL_IPV4 + "|(?:" + URL_LABEL + "\\.)++[" + URL_LETTER + "]{2,63}|localhost)"
                + "(?::" + URL_PORT + ")?(?:[/?#]\\P{IsWhite_Space}*)?$", Pattern.CASE_INSENSITIVE);

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";
            String value = wrapper.getValue() + "";

            if (value.length() < 2083 && pattern.matcher(value).matches()) return "";
            return "WRONG_URL";
        };
    };

    /**
     * A date YYYY-MM-DD, also with "min" and "max" bounds since LIVR 2.5, see Dates
     */
    public static Function<List<Object>, Function> iso_date = Dates.iso_date;
}
