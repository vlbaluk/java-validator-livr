package livr;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by vladislavbaluk on 9/29/2017.
 */
public class Validator {
    Map<String, Function> DEFAULT_RULES = new HashMap<>();
    JSONObject livrRules = new JSONObject();
    Map<String, List<FunctionKeeper>> validators;
    Map<String, Function> validatorBuilders;
    JSONObject errors;
    boolean isPrepared = false;
    boolean isAutoTrim = false;
    Clock clock = Clock.systemUTC();
    private JSONParser parser = new JSONParser();


    public Validator(Map<String, Function> defaultRules) {
        DEFAULT_RULES = defaultRules;
    }

    public Validator init(JSONObject livrRules, boolean isAutoTrim) {
        this.isPrepared = false;
        this.livrRules = livrRules;
        this.validators = new HashMap<>();
        this.validatorBuilders = new HashMap<>();
        this.errors = null;
        this.isAutoTrim = isAutoTrim;

        this.registerRules(DEFAULT_RULES);

        return this;
    }

    public Validator init(String livrRules, boolean isAutoTrim) throws ParseException {
        this.isPrepared = false;

        this.livrRules = (JSONObject) parser.parse(livrRules);

        this.validators = new HashMap<>();
        this.validatorBuilders = new HashMap<>();
        this.errors = null;
        this.isAutoTrim = isAutoTrim;

        this.registerRules(DEFAULT_RULES);

        return this;
    }

    public Validator() {
    }

    public Validator registerDefaultRules(Map<String, Function> rules) {
        DEFAULT_RULES.putAll(rules);
        return this;
    }

    public Map<String, Function> getDefaultRules() {
        return DEFAULT_RULES;
    }

    /**
     * The clock for "today", "yesterday" and "tomorrow" of the date rules, Clock.systemUTC() by
     * default. Java cannot replace the system clock in tests, so they set a fixed clock here:
     * LIVR.validator().setClock(Clock.fixed(Instant.parse("2026-10-18T12:00:00Z"), ZoneOffset.UTC)).
     * The dates are always in UTC, whatever the zone of the clock. Set it before the validator
     * is prepared: nested rules get it when they are built
     */
    public Validator setClock(Clock clock) {
        this.clock = clock;
        return this;
    }

    public Clock getClock() {
        return clock;
    }

    public void registerAliasedDefaultRule(JSONObject alias) throws IOException {
        if (((String) alias.get("name")).isEmpty()) throw new IOException("Alias name required");
        String name = (String) alias.get("name");
        DEFAULT_RULES.put(name, _buildAliasedRule(alias));
    }

    Function _buildAliasedRule(JSONObject alias) throws IOException {
        if (((String) alias.get("name")).isEmpty()) throw new IOException("Alias name required");
        if (alias.get("rules") == null) throw new IOException("Alias rules required");

        JSONObject livr = new JSONObject();
        Object rules = alias.get("rules");
        livr.put("value", rules);

        Function<List<Object>, Function> aliasFunction = objects -> {
            Map<String, Function> ruleBuilders = (Map<String, Function>) ((objects.size() > 1) ? objects.get(1) : objects.get(0));
            try {
                Validator validator = new Validator(ruleBuilders).setClock(RuleArguments.clock(objects)).init(livr, false).prepare();
                return (Function<FunctionKeeper, Object>) (wrapper) -> {
                    // An alias is transparent: its rules see the containing object of the field
                    Object error = validator.validateValue(wrapper.getValue(), wrapper.getArgs(), wrapper.getFieldResultArr());
                    if ("".equals(error)) return "";
                    return alias.get("error") == null ? error : alias.get("error");
                };
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };
        return aliasFunction;
    }

    public Validator prepare() throws IOException {
        JSONObject allRules = this.livrRules;

        for (String field : (Set<String>) allRules.keySet()) {
            Object fieldRules = allRules.get(field);
            JSONArray rules1 = null;
            if (fieldRules.getClass() == JSONArray.class) {
                rules1 = (JSONArray) fieldRules;
            }
            if (fieldRules.getClass() == JSONObject.class) {
                rules1 = new JSONArray();
                rules1.add(fieldRules);
            }
            if (fieldRules.getClass() == String.class) {
                rules1 = new JSONArray();
                JSONObject json = new JSONObject();
                json.put(fieldRules, new JSONArray());
                rules1.add(json);
            }
            if (rules1 == null)
                return this;

            List<FunctionKeeper> validators = new ArrayList<>();

            for (int i = 0; i < rules1.size(); i++) {
                Map parsed = this._parseRule(rules1.get(i));
                validators.add(this._buildValidator((String) parsed.get("name"), (List<Object>) parsed.get("args"),
                        (List<Object>) parsed.get("arguments")));
            }

            this.validators.put(field, validators);
        }

        this.isPrepared = true;
        return this;
    }

    public JSONObject validate(Object str) throws IOException {
        try {
            if (str == null) {
                throw new ParseException(0);
            }
            return validate((JSONObject) parser.parse(str + ""));
        } catch (ParseException e) {
            this.errors = new JSONObject();
            this.errors.put("base", "FORMAT_ERROR");
            return null;
        }
    }

    public JSONObject validate(JSONObject data) throws IOException, ParseException {
        if (!this.isPrepared) this.prepare();

        if (this.isAutoTrim) {
            data = (JSONObject) _autoTrim(data);
        }

        JSONObject errors = new JSONObject();
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> containingObject = toMap(data);

        for (String k : validators.keySet()) {
            Object value = data.get(k);
            Object error = _validateField(k, value, data.containsKey(k), containingObject, result);
            if (error != null) errors.put(k, fieldError(error, value));
        }

        if (LIVRUtils.isEmptyObject(errors)) {
            this.errors = null;

            return (JSONObject) new JSONParser().parse(JSONObject.toJSONString(result));
        } else {
            this.errors = errors;
            return null;
        }
    }

    /**
     * Validates one value with the rules of the only field of this validator: the rules of an
     * alias, an "or" branch or a metarule. The rules see containingObject as the object of the
     * field (LIVR 2.5, 2.5). Returns "" and adds the output, if any, to output, or returns the
     * error: an error code or the errors of a metarule
     */
    public Object validateValue(Object value, Map containingObject, List<Object> output) {
        String field = validators.keySet().iterator().next();
        Map<String, Object> result = new HashMap<>(2);

        Object error = _validateField(field, value, value != null, containingObject, result);
        if (error != null) return error;

        if (result.containsKey(field)) output.add(result.get(field));
        return "";
    }

    /**
     * Runs the rules of a field in order, the output of a rule is the input of the next one.
     * Puts the output into result and returns null, or returns the error of the first failing rule
     */
    Object _validateField(String field, Object value, boolean isPresent, Map containingObject, Map<String, Object> result) {
        for (FunctionKeeper v : validators.get(field)) {
            v.setFieldResultArr(new ArrayList<>());
            v.setValue(result.containsKey(field) ? result.get(field) : value);
            v.setArgs(containingObject);
            Object errCode = v.getFunction().apply(v);
            if (errCode != null && !errCode.toString().isEmpty()) {
                return errCode;
            } else if (!v.getFieldResultArr().isEmpty()) {
                result.put(field, v.getFieldResultArr().get(0));
            } else if (isPresent && !result.containsKey(field)) {
                result.put(field, value);
            }
        }
        return null;
    }

    /**
     * The error of a field as this library reports it: an error code as {"code": ..., "fieldValue": ...},
     * the errors of a metarule as they are
     */
    public static Object fieldError(Object error, Object value) {
        if (!(error instanceof String)) return error;

        JSONObject fieldError = new JSONObject();
        fieldError.put("code", error);
        fieldError.put("fieldValue", value);
        return fieldError;
    }

    public JSONObject getErrors() {
        return this.errors;
    }

    public Validator registerRules(Map<String, Function> rules) {
        validatorBuilders.putAll(rules);
        return this;
    }

    public void registerAliasedRule(String alias) throws IOException, ParseException {
        registerAliasedRule((JSONObject) parser.parse(alias));
    }

    public void registerAliasedRule(JSONObject alias) throws IOException {
        if (((String) alias.get("name")).isEmpty()) throw new IOException("Alias name required");
        String name = (String) alias.get("name");
        this.validatorBuilders.put(name, _buildAliasedRule(alias));
    }

    public Map<String, Function> getRules() {
        return this.validatorBuilders;
    }

    public Map _parseRule(Object livrRule) {
        String name;
        List<Object> args = new ArrayList<>();
        List<Object> arguments = new ArrayList<>();

        if (LIVRUtils.isObject(livrRule)) {
            name = (String) ((JSONObject) livrRule).keySet().iterator().next();
            Object args1 = ((JSONObject) livrRule).get(name);
            arguments = args1 instanceof JSONArray ? (JSONArray) args1 : Lists.newArrayList(args1);

            if (args1 != null && args1.getClass() == JSONArray.class) {
                args1 = removeRedundantBracers((JSONArray) args1);
                args = Lists.newArrayList();
                args.add(args1);
            } else args = Lists.newArrayList(args1);
        } else {
            name = (String) livrRule;
        }
        Map map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("args", args);
        map.put("arguments", arguments);
        return map;
    }

    public Object removeRedundantBracers(JSONArray jsonArray) {
        if (jsonArray.size() == 1 && jsonArray.get(0) instanceof JSONArray) {
            return removeRedundantBracers((JSONArray) jsonArray.get(0));
        } else if (jsonArray.size() == 1 && !(jsonArray.get(0) instanceof JSONArray)) {
            return jsonArray.get(0);
        }
        return jsonArray;
    }

    public FunctionKeeper _buildValidator(String name, List<Object> args) throws IOException {
        List<Object> arguments = args.isEmpty() ? new ArrayList<>()
                : args.get(0) instanceof JSONArray ? (JSONArray) args.get(0) : Lists.newArrayList(args.get(0));
        return _buildValidator(name, args, arguments);
    }

    /**
     * Builds a rule. args are the arguments for the LIVR 2.0 rules (a single list argument
     * unwrapped), arguments are the arguments as written in the schema, see RuleArguments
     */
    public FunctionKeeper _buildValidator(String name, List<Object> args, List<Object> arguments) throws IOException {

        if (this.validatorBuilders.get(name) == null) {
            throw new IOException("Rule [" + name + "] not registered");
        }

        Function func = this.validatorBuilders.get(name);

        args.add(this.getRules());
        func = (Function) func.apply(new RuleArguments(args, arguments, clock));

        return new FunctionKeeper(null, func);
    }

    public static Object _autoTrim(Object data) {
        if (data == null) {
            return data;
        }

        Class dataType = data.getClass();

        if (dataType == String.class) {
            return ((String) data).trim();
        } else if (dataType == JSONArray.class) {
            JSONArray trimmedData = new JSONArray();

            for (Object entry : ((JSONArray) data).toArray()) {
                trimmedData.add(Validator._autoTrim(entry));
            }
            return trimmedData;
        } else if (dataType == JSONObject.class) {
            JSONObject trimmedData = new JSONObject();

            for (Object key : ((JSONObject) data).keySet()) {
                trimmedData.put(key, Validator._autoTrim(((JSONObject) data).get(key)));
            }
            return trimmedData;
        }

        return data;
    }


    public static Map<String, Object> toMap(JSONObject object) {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keySet().iterator();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.size(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
