package livr.Rules;

import livr.FunctionKeeper;
import livr.LIVRUtils;
import livr.RuleArguments;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * The list rules of LIVR 2.5
 */
public class ListRules {

    /**
     * The number of items: {"list_length": 2}, {"list_length": [1, 10]}, {"list_length": {"max": 100}}
     */
    public static Function<List<Object>, Function> list_length = objects -> {
        List<Object> args = RuleArguments.get(objects);
        if (args.isEmpty()) throw RuleArguments.schemaError("\"list_length\" needs a length");

        final int min;
        final int max;
        if (args.get(0) instanceof JSONObject) {
            Map<?, ?> options = (Map<?, ?>) args.get(0);
            if (options.isEmpty()) throw RuleArguments.schemaError("\"list_length\" needs \"min\" or \"max\"");
            for (Object key : options.keySet()) {
                if (!key.equals("min") && !key.equals("max")) {
                    throw RuleArguments.schemaError("unknown option \"" + key + "\" of \"list_length\"");
                }
            }
            min = options.containsKey("min") ? RuleArguments.nonNegativeInteger(options.get("min"), "list_length") : 0;
            max = options.containsKey("max") ? RuleArguments.nonNegativeInteger(options.get("max"), "list_length") : Integer.MAX_VALUE;
        } else {
            min = RuleArguments.nonNegativeInteger(args.get(0), "list_length");
            max = args.size() > 1 ? RuleArguments.nonNegativeInteger(args.get(1), "list_length") : min;
        }
        if (min > max) throw RuleArguments.schemaError("\"min\" of \"list_length\" is greater than \"max\"");

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isEmpty(wrapper.getValue())) return "";
            if (!(wrapper.getValue() instanceof List)) return "FORMAT_ERROR";

            int size = ((List) wrapper.getValue()).size();
            if (size < min) return "TOO_FEW_ITEMS";
            if (size > max) return "TOO_MANY_ITEMS";
            return "";
        };
    };

    /**
     * No duplicate items, compared as strings: "unique" for primitive items, {"unique": ["country", "zip"]}
     * for objects with the same values of these keys. Empty items are ignored
     */
    public static Function<List<Object>, Function> unique = objects -> {
        List<Object> args = RuleArguments.get(objects);
        List<String> keys = args.isEmpty() ? new ArrayList<>() : RuleArguments.strings(objects, "unique");

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isEmpty(wrapper.getValue())) return "";
            if (!(wrapper.getValue() instanceof List)) return "FORMAT_ERROR";

            // A wrong item gives FORMAT_ERROR even after duplicates, so check every item
            Set<Object> seen = new HashSet<>();
            boolean hasDuplicates = false;
            for (Object item : (List<?>) wrapper.getValue()) {
                if (LIVRUtils.isEmpty(item)) continue;

                Object itemKey;
                if (keys.isEmpty()) {
                    if (!LIVRUtils.isPrimitive(item)) return "FORMAT_ERROR";
                    itemKey = LIVRUtils.stringForm(item);
                } else {
                    if (!(item instanceof Map)) return "FORMAT_ERROR";

                    List<String> values = new ArrayList<>(keys.size());
                    boolean hasEmptyValue = false;
                    for (String key : keys) {
                        Object value = ((Map<?, ?>) item).get(key);
                        if (LIVRUtils.isEmpty(value)) {
                            hasEmptyValue = true;
                        } else if (!LIVRUtils.isPrimitive(value)) {
                            return "FORMAT_ERROR";
                        } else {
                            values.add(LIVRUtils.stringForm(value));
                        }
                    }
                    if (hasEmptyValue) continue;
                    itemKey = values;
                }

                if (!seen.add(itemKey)) hasDuplicates = true;
            }

            return hasDuplicates ? "NOT_UNIQUE_ITEMS" : "";
        };
    };
}
