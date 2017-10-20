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
                Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
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

            if (!value.equals(wrapper.getArgs().get(field))) return "FIELDS_NOT_EQUAL";
            return "";
        };
    };

    public static Function<List<Object>, Function> url = objects -> {
        Pattern pattern = Pattern.compile("(?i)^(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?$", Pattern.CASE_INSENSITIVE);

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

        if (value.matches("^([0-9]{4})(-?)(1[0-2]|0[1-9])\\2(3[01]|0[1-9]|[12][0-9])$")) {
            return "";
        }
        return "WRONG_DATE";

    };
}
