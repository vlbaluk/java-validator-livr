package livr.Rules;

import livr.FunctionKeeper;
import livr.LIVRUtils;
import org.json.simple.JSONObject;

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
        if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class) return "";
        wrapper.getFieldResultArr().add((wrapper.getValue() + "").trim());

        return "";
    };

    public static Function<List<Object>, Function> to_lc = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class) return "";
        wrapper.getFieldResultArr().add((wrapper.getValue() + "").toLowerCase());

        return "";
    };

    public static Function<List<Object>, Function> to_uc = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class) return "";
        wrapper.getFieldResultArr().add((wrapper.getValue() + "").toUpperCase());

        return "";
    };

    public static Function<List<Object>, Function> remove = objects -> {
        String escaped = Pattern.quote(objects.get(0) + "");

        String chars = "[" + escaped + "]";

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class) return "";
            wrapper.getFieldResultArr().add((wrapper.getValue() + "").replaceAll(chars, ""));

            return "";
        };
    };


    public static Function<List<Object>, Function> leave_only = objects -> {
        String escaped = Pattern.quote(objects.get(0) + "");

        String chars = "[^" + escaped + "]";

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class) return "";
            wrapper.getFieldResultArr().add((wrapper.getValue() + "").replaceAll(chars, ""));

            return "";
        };
    };
}
