package livr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
 * @author vladislavbaluk (creator)
 * @author Gábor KOLÁROVICS
 * 
 * @since 2017/09/29
 */
public class Validator {
	Map<String, Function> DEFAULT_RULES = new HashMap<>();
	JSONObject livrRules = new JSONObject();
	Map<String, List<FunctionKeeper>> validators;
	Map<String, Function> validatorBuilders;
	JSONObject errors;
	boolean isPrepared = false;
	boolean isAutoTrim = false;
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

	public void registerAliasedDefaultRule(JSONObject alias) throws IOException {
		if (((String) alias.get("name")).isEmpty())
			throw new IOException("Alias name required");
		String name = (String) alias.get("name");
		DEFAULT_RULES.put(name, _buildAliasedRule(alias));
	}

	Function _buildAliasedRule(JSONObject alias) throws IOException {
		if (((String) alias.get("name")).isEmpty())
			throw new IOException("Alias name required");
		if (alias.get("rules") == null)
			throw new IOException("Alias rules required");

		JSONObject livr = new JSONObject();
		Object rules = alias.get("rules");
		livr.put("value", rules);

		Function<List<Object>, Function> aliasFunction = objects -> {
			Map<String, Function> ruleBuilders = (Map<String, Function>) ((objects.size() > 1) ? objects.get(1)
					: objects.get(0));
			try {
				Validator validator = new Validator(ruleBuilders).init(livr, false).prepare();
				return (Function<FunctionKeeper, Object>) (wrapper) -> {
					try {
						JSONObject json = new JSONObject();
						json.put("value", wrapper.getValue());
						JSONObject result = validator.validate(json);

						if (result != null) {
							wrapper.getFieldResultArr().add(result.get("value"));
							return "";
						} else {
							return alias.get("error") == null ? validator.getErrors().get("value") : alias.get("error");
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					return null;
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
				validators.add(this._buildValidator((String) parsed.get("name"), (List<Object>) parsed.get("args")));
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
		if (data == null) {
			this.errors = new JSONObject();
			this.errors.put("base", "FORMAT_ERROR");
			return null;
		}

		if (!this.isPrepared)
			this.prepare();

		if (this.isAutoTrim) {
			data = (JSONObject) _autoTrim(data);
		}

		JSONObject errors = new JSONObject();
		Map<String, Object> result = new HashMap<>();

		Set<String> dataKeys = (Set<String>) data.keySet();

		for (String k : validators.keySet()) {
			Object value = data.get(k);
			List<FunctionKeeper> valids = validators.get(k);
			for (FunctionKeeper v : valids) {
				v.setFieldResultArr(new ArrayList<>());
				v.setValue(result.get(k) != null ? result.get(k) : value);
				v.setArgs(toMap(data));
				Object errCode = v.getFunction().apply(v);
				if (errCode != null && !errCode.toString().isEmpty()) {
					errors.put(k, errCode);
					break;
				} else if (!v.getFieldResultArr().isEmpty()) {
					result.put(k, v.getFieldResultArr().get(0));
				} else if (!result.containsKey(k) && dataKeys.contains(k)) {
					result.put(k, value);
				}
			}
		}

		if (LIVRUtils.isEmptyObject(errors)) {
			this.errors = null;
			return (JSONObject) new JSONParser().parse(JSONObject.toJSONString(result));
		} else {
			this.errors = errors;
			return null;
		}
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
		if (((String) alias.get("name")).isEmpty())
			throw new IOException("Alias name required");
		String name = (String) alias.get("name");
		this.validatorBuilders.put(name, _buildAliasedRule(alias));
	}

	public Map<String, Function> getRules() {
		return this.validatorBuilders;
	}

	public Map _parseRule(Object livrRule) {
		String name;
		List<Object> args = new ArrayList<>();

		if (LIVRUtils.isObject(livrRule)) {
			name = (String) ((JSONObject) livrRule).keySet().iterator().next();
			Object args1 = ((JSONObject) livrRule).get(name);

			if (args1.getClass() == JSONArray.class) {
				args1 = removeRedundantBracers((JSONArray) args1);
				List<Object> arrayList = new ArrayList<>();
				Collections.addAll(arrayList, args1);
				args = arrayList;
			} else {
				List<Object> arrayList = new ArrayList<>();
				Collections.addAll(arrayList, args1);
				args = arrayList;
			}
		} else {
			name = (String) livrRule;
		}
		Map map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("args", args);
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

		if (this.validatorBuilders.get(name) == null) {
			throw new IOException("Rule [" + name + "] not registered");
		}

		Function func = this.validatorBuilders.get(name);

		args.add(this.getRules());
		func = (Function) func.apply(args);

		return new FunctionKeeper(null, func);
	}

	public static Object _autoTrim(Object data) {
		Class dataType = data.getClass();

		if (dataType != JSONObject.class) {
			return (data + "").trim();
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
