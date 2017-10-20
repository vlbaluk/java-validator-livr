package livr.Rules;

import livr.FunctionKeeper;
import livr.LIVRUtils;
import org.json.simple.JSONArray;

import java.util.List;
import java.util.function.Function;

/**
 * Created by vladislavbaluk on 9/28/2017.
 */
public class CommonRules {

    public static Function<List<Object>, Function> required = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue())) {
            return "REQUIRED";
        }
        return "";
    };

    public static Function<List<Object>, Function> not_empty = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (wrapper.getValue() != null && wrapper.getValue().equals("")) {
            return "CANNOT_BE_EMPTY";
        }
        return "";
    };

    public static Function<List<Object>, Function> not_empty_list = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue())) return "CANNOT_BE_EMPTY";
        if (!(wrapper.getValue() instanceof JSONArray)) return "FORMAT_ERROR";
        if (((JSONArray) wrapper.getValue()).size() == 0) return "CANNOT_BE_EMPTY";

        return "";
    };

    public static Function<List<Object>, Function> any_object = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue((wrapper.getValue()))) return "";

        if (!LIVRUtils.isObject(wrapper.getValue())) {
            return "FORMAT_ERROR";
        }
        return "";
    };

}
