package livr.Rules;

import livr.FunctionKeeper;
import livr.LIVRUtils;
import livr.RuleArguments;
import org.json.simple.JSONArray;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Created by vladislavbaluk on 9/28/2017.
 */
public class Modifiers {

    public static Function<List<Object>, Function> default1 = objects -> {
        final Object defaultValue = objects.get(0);
        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isNoValue(wrapper.getValue())) {
                wrapper.getFieldResultArr().add(defaultValue);
                return "";
            }
            return "";
        };
    };

    public static Function<List<Object>, Function> trim = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue()) || !LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "";
        wrapper.getFieldResultArr().add((wrapper.getValue() + "").trim());

        return "";
    };

    public static Function<List<Object>, Function> to_lc = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue()) || !LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "";
        wrapper.getFieldResultArr().add((wrapper.getValue() + "").toLowerCase());

        return "";
    };

    public static Function<List<Object>, Function> to_uc = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue()) || !LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "";
        wrapper.getFieldResultArr().add((wrapper.getValue() + "").toUpperCase());

        return "";
    };

    public static Function<List<Object>, Function> remove = objects -> {
        String escaped = Pattern.quote(objects.get(0) + "");

        String chars = "[" + escaped + "]";

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isNoValue(wrapper.getValue()) || !LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "";
            wrapper.getFieldResultArr().add((wrapper.getValue() + "").replaceAll(chars, ""));

            return "";
        };
    };


    public static Function<List<Object>, Function> leave_only = objects -> {
        String escaped = Pattern.quote(objects.get(0) + "");

        String chars = "[^" + escaped + "]";

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isNoValue(wrapper.getValue()) || !LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "";
            wrapper.getFieldResultArr().add((wrapper.getValue() + "").replaceAll(chars, ""));

            return "";
        };
    };

    /**
     * Splits a string (or a number or a boolean in its string form) at every separator, empty
     * parts are kept. A list, an object and an empty value stay as they are (LIVR 2.5)
     */
    public static Function<List<Object>, Function> split = objects -> {
        List<Object> args = RuleArguments.get(objects);
        if (args.isEmpty()) throw RuleArguments.schemaError("\"split\" needs a separator");
        String separator = RuleArguments.string(args.get(0), "split");
        if (separator.isEmpty()) throw RuleArguments.schemaError("the separator of \"split\" must be a non-empty string");
        Pattern pattern = Pattern.compile(separator, Pattern.LITERAL);

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isEmpty(wrapper.getValue()) || !LIVRUtils.isPrimitive(wrapper.getValue())) return "";

            JSONArray parts = new JSONArray();
            parts.addAll(Arrays.asList(pattern.split(LIVRUtils.stringForm(wrapper.getValue()), -1)));
            wrapper.getFieldResultArr().add(parts);
            return "";
        };
    };
}
