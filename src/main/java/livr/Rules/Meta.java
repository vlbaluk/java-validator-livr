package livr.Rules;

import com.google.common.collect.Lists;
import livr.FunctionKeeper;
import livr.LIVRUtils;
import livr.Reference;
import livr.RuleArguments;
import livr.Validator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * Created by vladislavbaluk on 9/28/2017.
 */
public class Meta {
    public static Function<List<Object>, Function> nested_object = (List<Object> objects) -> {
        try {
            Validator validator = new Validator((Map<String, Function>) objects.get(1)).setClock(RuleArguments.clock(objects)).init((JSONObject) firstArgument(objects), false).prepare();

            return (Function<FunctionKeeper, Object>) (wrapper) -> {
                if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
                if (!LIVRUtils.isObject(wrapper.getValue())) return "FORMAT_ERROR";

                try {
                    JSONObject result = validator.validate(wrapper.getValue());

                    if (result != null) {
                        wrapper.getFieldResultArr().add(result);
                        return "";
                    } else {
                        return validator.getErrors();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    };

    public static Function<List<Object>, Function> list_of = (List<Object> objects) -> {
        try {
            JSONObject field = new JSONObject();
            JSONArray array = new JSONArray();
            Object rules = listOfRules(objects);
            array.addAll(Lists.newArrayList(rules));
            if (rules.getClass() == JSONArray.class) {
                field.put("field", rules);
            } else {
                field.put("field", array);
            }
            Validator validator = new Validator((Map<String, Function>) objects.get(1)).setClock(RuleArguments.clock(objects)).init(field, false).prepare();

            return (Function<FunctionKeeper, Object>) (wrapper) -> {
                if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
                if (!(wrapper.getValue() instanceof JSONArray)) return "FORMAT_ERROR";

                boolean hasErrors = false;
                JSONArray results = new JSONArray();
                JSONArray errors = new JSONArray();
                Object[] arr = ((JSONArray) wrapper.getValue()).toArray();
                for (Object value : arr) {
                    // An item has no containing object of its own (LIVR 2.5, 2.5)
                    List<Object> output = new ArrayList<>(1);
                    Object error = validator.validateValue(value, Collections.emptyMap(), output);
                    if ("".equals(error)) {
                        results.add(output.isEmpty() ? value : output.get(0));
                        errors.add(null);
                    } else {
                        hasErrors = true;
                        errors.add(Validator.fieldError(error, value));
                        results.add(null);
                    }

                }
                if (hasErrors) {
                    return errors;
                } else {
                    wrapper.getFieldResultArr().add(results);
                    return "";
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    };

    public static Function<List<Object>, Function> list_of_objects = (List<Object> objects) -> {
        try {
            Validator validator = new Validator((Map<String, Function>) objects.get(1)).setClock(RuleArguments.clock(objects)).init((JSONObject) firstArgument(objects), false).prepare();

            return (Function<FunctionKeeper, Object>) (wrapper) -> {
                if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
                if (!(wrapper.getValue() instanceof JSONArray)) return "FORMAT_ERROR";

                try {
                    boolean hasErrors = false;
                    JSONArray results = new JSONArray();
                    JSONArray errors = new JSONArray();
                    for (Object value : ((JSONArray) wrapper.getValue()).toArray()) {

                        if (!LIVRUtils.isObject(value)) {
                            errors.add("FORMAT_ERROR");
                            hasErrors = true;
                            continue;
                        }
                        JSONObject result = validator.validate(value);

                        if (result != null) {
                            results.add(result);
                            errors.add(null);
                        } else {
                            hasErrors = true;
                            errors.add(validator.getErrors());
                            results.add(null);
                        }
                    }
                    if (hasErrors) {
                        return errors;
                    } else {
                        wrapper.getFieldResultArr().add(results);
                        return "";
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    };

    public static Function<List<Object>, Function> list_of_different_objects = (List<Object> objects) -> {
        try {
            Map<Object, Validator> validators = new HashMap<>();
            Iterator it = ((JSONArray) objects.get(0)).iterator();
            Object selectorField = it.next();
            JSONObject values = (JSONObject) it.next();
            for (Object key : values.keySet()) {
                JSONObject selectorValue = (JSONObject) values.get(key);

                Validator validator = new Validator((Map<String, Function>) objects.get(1)).setClock(RuleArguments.clock(objects)).init(selectorValue, false)
                        .registerRules((Map<String, Function>) objects.get(1))
                        .prepare();

                validators.put(key, validator);
            }

            return (Function<FunctionKeeper, Object>) (wrapper) -> {
                if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
                if (!(wrapper.getValue() instanceof JSONArray)) return "FORMAT_ERROR";

                try {
                    boolean hasErrors = false;
                    JSONArray results = new JSONArray();
                    JSONArray errors = new JSONArray();

                    for (Object value : ((JSONArray) wrapper.getValue()).toArray()) {


                        if (!LIVRUtils.isObject(value) || ((JSONObject) value).get(selectorField) == null || validators.get(((JSONObject) value).get(selectorField)) == null) {
                            errors.add("FORMAT_ERROR");
                            hasErrors = true;
                            continue;
                        }

                        Validator validator = validators.get(((JSONObject) value).get(selectorField));

                        JSONObject result = validator.validate((JSONObject) value);
                        if (result != null) {
                            results.add(result);
                            errors.add(null);
                        } else {
                            hasErrors = true;
                            errors.add(validator.getErrors());
                            results.add(null);
                        }
                    }
                    if (hasErrors) {
                        return errors;
                    } else {
                        wrapper.getFieldResultArr().add(results);
                        return "";
                    }

                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
                return null;
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    };


    public static Function<List<Object>, Function> variable_object = (List<Object> objects) -> {
        try {
            Map<Object, Validator> validators = new HashMap<>();
            Iterator it = ((JSONArray) objects.get(0)).iterator();
            Object selectorField = it.next();
            JSONObject values = (JSONObject) it.next();
            for (Object key : values.keySet()) {
                JSONObject selectorValue = (JSONObject) values.get(key);

                Validator validator = new Validator((Map<String, Function>) objects.get(1)).setClock(RuleArguments.clock(objects)).init(selectorValue, false)
                        .registerRules((Map<String, Function>) objects.get(1))
                        .prepare();

                validators.put(key, validator);
            }

            return (Function<FunctionKeeper, Object>) (wrapper) -> {
                if (LIVRUtils.isNoValue(wrapper.getValue())) return "";

                try {
                    Object value = wrapper.getValue();
                    if (!LIVRUtils.isObject(value) || ((JSONObject) value).get(selectorField) == null || validators.get(((JSONObject) value).get(selectorField)) == null) {
                        return "FORMAT_ERROR";
                    }

                    Validator validator = validators.get(((JSONObject) value).get(selectorField));

                    JSONObject result = validator.validate(value);
                    if (result != null) {
                        wrapper.getFieldResultArr().add(result);
                        return "";
                    } else {
                        return validator.getErrors();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    };


    public static Function<List<Object>, Function> or = (List<Object> objects) -> {
        try {
            List<Validator> validators = new ArrayList<>();
            for (Object entry : ((JSONArray) objects.get(0)).toArray()) {
                JSONObject field = new JSONObject();
                field.put("field", entry);
                Validator validator = new Validator((Map<String, Function>) objects.get(1)).setClock(RuleArguments.clock(objects)).init(field, false).prepare();
                validators.add(validator);
            }

            return (Function<FunctionKeeper, Object>) (wrapper) -> {
                Object lastError = null;
                for (Validator validator : validators) {
                    // A branch is transparent: its rules see the containing object of the field
                    lastError = validator.validateValue(wrapper.getValue(), wrapper.getArgs(), wrapper.getFieldResultArr());
                    if ("".equals(lastError)) return "";
                }
                return lastError;
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    };

    /**
     * Applies rules depending on another field (LIVR 2.5): {"when": [REFERENCE, CONDITION,
     * RULES_IF_TRUE, RULES_IF_FALSE]} (RULES_IF_FALSE is optional) or {"when": [REFERENCE, CASES]}
     */
    public static Function<List<Object>, Function> when = (List<Object> objects) -> {
        List<Object> args = RuleArguments.get(objects);
        if (args.size() < 2) throw RuleArguments.schemaError("\"when\" needs a reference and rules");

        Reference reference = new Reference(args.get(0), "when");

        if (args.size() == 2) {
            if (!(args.get(1) instanceof JSONObject) || ((JSONObject) args.get(1)).isEmpty()) {
                throw RuleArguments.schemaError("the cases of \"when\" must be an object with at least one key");
            }
            Map<String, Validator> cases = new HashMap<>();
            for (Object key : ((JSONObject) args.get(1)).keySet()) {
                cases.put((String) key, valueValidator(((JSONObject) args.get(1)).get(key), objects, "when"));
            }

            return (Function<FunctionKeeper, Object>) (wrapper) -> {
                Object referenced = reference.resolve(wrapper.getArgs());
                Validator validator = LIVRUtils.isPrimitive(referenced) && !LIVRUtils.isEmpty(referenced)
                        ? cases.get(LIVRUtils.stringForm(referenced))
                        : null;

                // No case can be chosen: only an empty value passes, an unchecked value would pass otherwise
                if (validator == null) return LIVRUtils.isEmpty(wrapper.getValue()) ? "" : "NOT_ALLOWED_VALUE";

                return validator.validateValue(wrapper.getValue(), wrapper.getArgs(), wrapper.getFieldResultArr());
            };
        }

        Validator condition = valueValidator(args.get(1), objects, "when");
        Validator ifTrue = valueValidator(args.get(2), objects, "when");
        Validator ifFalse = args.size() > 3 ? valueValidator(args.get(3), objects, "when") : null;

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            // The condition rules see the containing object of the validated field, their output is discarded
            Object referenced = reference.resolve(wrapper.getArgs());
            boolean isTrue = !LIVRUtils.isEmpty(referenced)
                    && "".equals(condition.validateValue(referenced, wrapper.getArgs(), new ArrayList<>(1)));

            Validator validator = isTrue ? ifTrue : ifFalse;
            if (validator == null) return "";

            return validator.validateValue(wrapper.getValue(), wrapper.getArgs(), wrapper.getFieldResultArr());
        };
    };

    /**
     * Validates every value of an object with the rules, as list_of validates every item (LIVR 2.5)
     */
    public static Function<List<Object>, Function> values = (List<Object> objects) -> {
        Validator validator = valueValidator(listOfRules(objects), objects, "values");

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isEmpty(wrapper.getValue())) return "";
            if (!(wrapper.getValue() instanceof Map)) return "FORMAT_ERROR";

            JSONObject results = new JSONObject();
            JSONObject errors = new JSONObject();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) wrapper.getValue()).entrySet()) {
                // A value has no containing object of its own (LIVR 2.5, 2.5)
                List<Object> output = new ArrayList<>(1);
                Object error = validator.validateValue(entry.getValue(), Collections.emptyMap(), output);
                if ("".equals(error)) {
                    results.put(entry.getKey(), output.isEmpty() ? entry.getValue() : output.get(0));
                } else {
                    errors.put(entry.getKey(), Validator.fieldError(error, entry.getValue()));
                }
            }

            if (!errors.isEmpty()) return errors;
            wrapper.getFieldResultArr().add(results);
            return "";
        };
    };

    /**
     * Validates every key of an object as a string with the rules; they only check (LIVR 2.5)
     */
    public static Function<List<Object>, Function> keys = (List<Object> objects) -> {
        Validator validator = valueValidator(listOfRules(objects), objects, "keys");

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isEmpty(wrapper.getValue())) return "";
            if (!(wrapper.getValue() instanceof Map)) return "FORMAT_ERROR";

            JSONObject errors = new JSONObject();
            for (Object key : ((Map<?, ?>) wrapper.getValue()).keySet()) {
                // A key has no containing object of its own (LIVR 2.5, 2.5)
                Object error = validator.validateValue(key, Collections.emptyMap(), new ArrayList<>(1));
                if (!"".equals(error)) errors.put(key, Validator.fieldError("WRONG_KEY", key));
            }

            return errors.isEmpty() ? "" : errors;
        };
    };

    /**
     * Passes any JSON value, objects and lists too, and keeps it unchanged (LIVR 2.5)
     */
    public static Function<List<Object>, Function> any = (List<Object> objects) -> {
        RuleArguments.none(objects, "any");
        return (Function<FunctionKeeper, Object>) (wrapper) -> "";
    };

    /**
     * A validator of one value for the rules of "when", "values" and "keys": a rule name, a rule
     * object or a list of them. An empty list [], null and [] in a list are schema errors, "any"
     * means no rules (LIVR 2.5, 2.6)
     */
    static Validator valueValidator(Object rules, List<Object> objects, String ruleName) {
        boolean isRules = rules instanceof String || rules instanceof JSONObject
                || rules instanceof JSONArray && !((JSONArray) rules).isEmpty();
        if (rules instanceof JSONArray) {
            for (Object rule : (JSONArray) rules) {
                if (!(rule instanceof String || rule instanceof JSONObject)) isRules = false;
            }
        }
        if (!isRules) {
            throw RuleArguments.schemaError("\"" + ruleName + "\" needs rules: a rule name, a rule object or a non-empty list of rules");
        }

        JSONObject livr = new JSONObject();
        livr.put("value", rules);
        try {
            return new Validator(RuleArguments.ruleBuilders(objects)).setClock(RuleArguments.clock(objects)).init(livr, false).prepare();
        } catch (IOException e) {
            throw RuleArguments.schemaError(e.getMessage());
        }
    }

    /**
     * The first argument of a LIVR 2.0 rule: {"nested_object": [{...}, "extra"]} ignores "extra"
     */
    static Object firstArgument(List<Object> objects) {
        Object args = objects.get(0);
        return args instanceof JSONArray ? ((JSONArray) args).get(0) : args;
    }

    /**
     * The rules of list_of, values and keys: {"list_of": ["required", "string"]} or the single
     * list form {"list_of": [["required", "string"]]}; arguments after that list are ignored
     */
    static Object listOfRules(List<Object> objects) {
        Object rules = objects.get(0);
        if (rules instanceof JSONArray && !((JSONArray) rules).isEmpty() && ((JSONArray) rules).get(0) instanceof JSONArray) {
            return ((JSONArray) rules).get(0);
        }
        return rules;
    }
}
