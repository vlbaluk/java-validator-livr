package livr.Rules;

import livr.FunctionKeeper;
import livr.LIVRUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

    public static Function<List<Object>, Function> url = objects -> {
        Pattern pattern = Pattern.compile("^(?:https?://)(?:\\P{IsWhite_Space}+(?::\\P{IsWhite_Space}*)?@)?(?:(?:(?:[1-9]\\d?|1\\d\\d|2[0-1]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[0-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:[a-z\\u00a1-\\uffff0-9]++(?:-++[a-z\\u00a1-\\uffff0-9]++)*+)(?:\\.[a-z\\u00a1-\\uffff0-9]++(?:-++[a-z\\u00a1-\\uffff0-9]++)*+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,})))\\.?|localhost)(?::\\d{2,5})?(?:[/?#]\\P{IsWhite_Space}*)?$", Pattern.CASE_INSENSITIVE);

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";
            String value = wrapper.getValue() + "";

            if (value.length() < 2083 && pattern.matcher(value).matches()) return "";
            return "WRONG_URL";
        };
    };

    public static Function<List<Object>, Function> iso_date = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
        if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";
        String value = wrapper.getValue() + "";

        if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) return "WRONG_DATE";
        try {
            LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return "WRONG_DATE";
        }
        return "";

    };
}
