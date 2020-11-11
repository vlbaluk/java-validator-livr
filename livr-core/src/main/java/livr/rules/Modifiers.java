package livr.rules;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import livr.FunctionKeeper;
import livr.LIVRUtils;

import static livr.api.Constant.EMPTY;

/**
 * @author vladislavbaluk (creator)
 * @author Gábor KOLÁROVICS
 * 
 * @since 2017/09/28
 */
public class Modifiers {

	private Modifiers() {
		throw new IllegalStateException("Utility class");
	}

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> default1 = objects -> {
		final Object defaultValue = objects.get(0);
		return wrapper -> {
			if (LIVRUtils.isNoValue(wrapper.getValue())) {
				wrapper.getFieldResultArr().add(defaultValue);
				return EMPTY;
			}
			return EMPTY;
		};
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> trim = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class)
			return EMPTY;
		wrapper.getFieldResultArr().add((wrapper.getValue() + "").trim());

		return EMPTY;
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> to_lc = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class)
			return EMPTY;
		wrapper.getFieldResultArr().add((wrapper.getValue() + "").toLowerCase());

		return EMPTY;
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> to_uc = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class)
			return EMPTY;
		wrapper.getFieldResultArr().add((wrapper.getValue() + "").toUpperCase());

		return EMPTY;
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> remove = objects -> {
		String escaped = Pattern.quote(objects.get(0) + "");

		String chars = "[" + escaped + "]";

		return wrapper -> {
			if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class)
				return EMPTY;
			wrapper.getFieldResultArr().add((wrapper.getValue() + "").replaceAll(chars, ""));

			return EMPTY;
		};
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> leave_only = objects -> {
		String escaped = Pattern.quote(objects.get(0) + "");

		String chars = "[^" + escaped + "]";

		return wrapper -> {
			if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class)
				return EMPTY;
			wrapper.getFieldResultArr().add((wrapper.getValue() + "").replaceAll(chars, EMPTY));

			return EMPTY;
		};
	};
}
