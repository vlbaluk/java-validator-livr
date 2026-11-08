package livr.Rules;

import livr.FunctionKeeper;
import livr.LIVRUtils;
import livr.Reference;
import livr.RuleArguments;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    /**
     * true, false, 1, 0, "1", "0", "true" and "false"; the output is a real boolean (LIVR 2.5).
     * "boolean" is a Java keyword, so the field is boolean1, like default1
     */
    public static Function<List<Object>, Function> boolean1 = objects -> {
        RuleArguments.none(objects, "boolean");

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isEmpty(wrapper.getValue())) return "";
            if (!LIVRUtils.isPrimitive(wrapper.getValue())) return "FORMAT_ERROR";

            String value = LIVRUtils.stringForm(wrapper.getValue());
            if (value.equals("true") || value.equals("1")) {
                wrapper.getFieldResultArr().add(Boolean.TRUE);
            } else if (value.equals("false") || value.equals("0")) {
                wrapper.getFieldResultArr().add(Boolean.FALSE);
            } else {
                return "NOT_BOOLEAN";
            }
            return "";
        };
    };

    /**
     * The field is required when every referenced field equals the given value or one of the
     * listed values, compared as strings (LIVR 2.5): {"required_if": {"type": ["company", "ngo"]}}
     */
    public static Function<List<Object>, Function> required_if = objects -> {
        List<Object> args = RuleArguments.get(objects);
        if (args.isEmpty() || !(args.get(0) instanceof JSONObject) || ((JSONObject) args.get(0)).isEmpty()) {
            throw RuleArguments.schemaError("\"required_if\" needs an object with at least one field");
        }

        Map<Reference, Set<String>> conditions = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) args.get(0)).entrySet()) {
            List<Object> values = new ArrayList<>();
            if (entry.getValue() instanceof JSONArray) values.addAll((JSONArray) entry.getValue());
            else values.add(entry.getValue());

            Set<String> allowed = new HashSet<>();
            for (Object value : values) {
                if (!LIVRUtils.isPrimitive(value) || "".equals(value)) {
                    throw RuleArguments.schemaError("the values of \"required_if\" must be non-empty strings, numbers or booleans");
                }
                allowed.add(LIVRUtils.stringForm(value));
            }
            if (allowed.isEmpty()) throw RuleArguments.schemaError("\"required_if\" needs a non-empty list of values");

            conditions.put(new Reference(entry.getKey(), "required_if"), allowed);
        }

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (!LIVRUtils.isEmpty(wrapper.getValue())) return "";

            for (Map.Entry<Reference, Set<String>> condition : conditions.entrySet()) {
                Object referenced = condition.getKey().resolve(wrapper.getArgs());
                boolean matches = LIVRUtils.isPrimitive(referenced) && !LIVRUtils.isEmpty(referenced)
                        && condition.getValue().contains(LIVRUtils.stringForm(referenced));
                if (!matches) return "";
            }
            return "REQUIRED";
        };
    };
}
