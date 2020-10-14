package livr;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.function.Function;

/**
 * Created by vladislavbaluk on 10/19/2017.
 */
public final class MyFuncClass {

    public static Function<List<Object>, Function> my_trim = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class) return "";
        wrapper.getFieldResultArr().add((wrapper.getValue() + "").trim());

        return "";
    };

    public static Function<List<Object>, Function> my_lc = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class) return "";
        wrapper.getFieldResultArr().add((wrapper.getValue() + "").toLowerCase());

        return "";
    };

    public static Function<List<Object>, Function> my_ucfirst = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class) return "";
        wrapper.getFieldResultArr().add((wrapper.getValue() + "").substring(0, 1).toUpperCase() + (wrapper.getValue() + "").substring(1));

        return "";
    };

    public static Function patchRule(String ruleName, Function ruleBuilder) {

        return arguments -> {
            Function ruleValidator = (Function) ruleBuilder.apply(arguments);
            Object ruleArgs1 = null;
            if (((List<Object>) arguments).size() == 2)
                ruleArgs1 = ((List<Object>) arguments).get(0);
            JSONArray ruleArgs = new JSONArray();
            if (ruleArgs1 != null)
                ruleArgs.add(ruleArgs1);

            return (Function<FunctionKeeper, Object>) q -> {
                Object errorCode = ruleValidator.apply(q);
                if (errorCode != null) {
                    JSONObject rule = new JSONObject();
                    rule.put(ruleName, ruleArgs);
                    JSONObject json = new JSONObject();
                    json.put("code", errorCode);
                    json.put("rule", rule);
                    return json;
                }
                return "";
            };
        };
    }
}
