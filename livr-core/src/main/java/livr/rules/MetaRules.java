package livr.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import livr.FunctionKeeper;
import livr.LIVRUtils;
import livr.Validator;

/**
 * Created by vladislavbaluk on 9/28/2017.
 */
public class MetaRules {

	private static Logger log = LoggerFactory.getLogger(MetaRules.class);

	public static Function<List<Object>, Function> nested_object = (List<Object> objects) -> {
		try {
			Validator validator = new Validator((Map<String, Function>) objects.get(1))
					.init((JSONObject) objects.get(0), false).prepare();

			return (Function<FunctionKeeper, Object>) (wrapper) -> {
				if (LIVRUtils.isNoValue(wrapper.getValue()))
					return "";
				if (!LIVRUtils.isObject(wrapper.getValue()))
					return "FORMAT_ERROR";

				try {
					JSONObject result = validator.validate(wrapper.getValue());

					if (result != null) {
						wrapper.getFieldResultArr().add(result);
						return "";
					} else {
						return validator.getErrors();
					}
				} catch (IOException e) {
					log.error(e.getMessage(), e.getCause());
				}
				return null;
			};
		} catch (IOException e) {
			log.error(e.getMessage(), e.getCause());
		}
		return null;
	};

	public static Function<List<Object>, Function> list_of = (List<Object> objects) -> {
		try {
			JSONObject field = new JSONObject();
			JSONArray array = new JSONArray();

			List<Object> list = new ArrayList<>();
			list.add(objects.get(0));
			array.addAll(list);

			if (objects.get(0).getClass() == JSONArray.class) {
				field.put("field", objects.get(0));
			} else {
				field.put("field", array);
			}
			Validator validator = new Validator((Map<String, Function>) objects.get(1)).init(field, false).prepare();

			return (Function<FunctionKeeper, Object>) (wrapper) -> {
				if (LIVRUtils.isNoValue(wrapper.getValue()))
					return "";
				if (!(wrapper.getValue() instanceof JSONArray))
					return "FORMAT_ERROR";

				try {
					boolean hasErrors = false;
					JSONArray results = new JSONArray();
					JSONArray errors = new JSONArray();
					Object[] arr = ((JSONArray) wrapper.getValue()).toArray();
					for (Object value : arr) {
						JSONObject fieldv = new JSONObject();
						fieldv.put("field", value);
						JSONObject result = validator.validate(fieldv);
						if (result != null) {
							results.add(result.get("field"));
							errors.add(null);
						} else {
							hasErrors = true;
							errors.add(validator.getErrors().get("field"));
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
					log.error(e.getMessage(), e.getCause());
				}
				return null;
			};
		} catch (IOException e) {
			log.error(e.getMessage(), e.getCause());
		}
		return null;
	};

	public static Function<List<Object>, Function> list_of_objects = (List<Object> objects) -> {
		try {
			Validator validator = new Validator((Map<String, Function>) objects.get(1))
					.init((JSONObject) objects.get(0), false).prepare();

			return (Function<FunctionKeeper, Object>) (wrapper) -> {
				if (LIVRUtils.isNoValue(wrapper.getValue()))
					return "";
				if (!(wrapper.getValue() instanceof JSONArray))
					return "FORMAT_ERROR";

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
					log.error(e.getMessage(), e.getCause());
				}
				return null;
			};
		} catch (IOException e) {
			log.error(e.getMessage(), e.getCause());
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

				Validator validator = new Validator((Map<String, Function>) objects.get(1)).init(selectorValue, false)
						.registerRules((Map<String, Function>) objects.get(1)).prepare();

				validators.put(key, validator);
			}

			return (Function<FunctionKeeper, Object>) (wrapper) -> {
				if (LIVRUtils.isNoValue(wrapper.getValue()))
					return "";
				if (!(wrapper.getValue() instanceof JSONArray))
					return "FORMAT_ERROR";

				try {
					boolean hasErrors = false;
					JSONArray results = new JSONArray();
					JSONArray errors = new JSONArray();

					for (Object value : ((JSONArray) wrapper.getValue()).toArray()) {

						if (!LIVRUtils.isObject(value) || ((JSONObject) value).get(selectorField) == null
								|| validators.get(((JSONObject) value).get(selectorField)) == null) {
							errors.add("FORMAT_ERROR");
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
					log.error(e.getMessage(), e.getCause());
				}
				return null;
			};
		} catch (IOException e) {
			log.error(e.getMessage(), e.getCause());
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

				Validator validator = new Validator((Map<String, Function>) objects.get(1)).init(selectorValue, false)
						.registerRules((Map<String, Function>) objects.get(1)).prepare();

				validators.put(key, validator);
			}

			return (Function<FunctionKeeper, Object>) (wrapper) -> {
				if (LIVRUtils.isNoValue(wrapper.getValue()))
					return "";

				try {
					Object value = wrapper.getValue();
					if (!LIVRUtils.isObject(value) || ((JSONObject) value).get(selectorField) == null
							|| validators.get(((JSONObject) value).get(selectorField)) == null) {
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
					log.error(e.getMessage(), e.getCause());
				}
				return null;
			};
		} catch (IOException e) {
			log.error(e.getMessage(), e.getCause());
		}
		return null;
	};

	public static Function<List<Object>, Function> or = (List<Object> objects) -> {
		try {
			List<Validator> validators = new ArrayList<>();
			for (Object entry : ((JSONArray) objects.get(0)).toArray()) {
				JSONObject field = new JSONObject();
				field.put("field", entry);
				Validator validator = new Validator((Map<String, Function>) objects.get(1)).init(field, false)
						.prepare();
				validators.add(validator);
			}

			return (Function<FunctionKeeper, Object>) (wrapper) -> {
				if (LIVRUtils.isNoValue(wrapper.getValue()))
					return "";
				try {
					Object value = wrapper.getValue();
					Object lastError = null;
					for (Validator validator : validators) {
						JSONObject valValue = new JSONObject();
						valValue.put("field", value);
						JSONObject result = validator.validate(valValue);
						if (result != null) {
							wrapper.getFieldResultArr().add(result.get("field"));
							return "";
						} else {
							lastError = validator.getErrors().get("field");
						}
					}
					if (lastError != null) {
						return lastError;
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e.getCause());
				}
				return null;
			};
		} catch (IOException e) {
			log.error(e.getMessage(), e.getCause());
		}
		return null;
	};
}