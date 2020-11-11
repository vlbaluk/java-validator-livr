package livr.rules;

import java.util.List;
import java.util.function.Function;

import org.json.simple.JSONArray;

import livr.FunctionKeeper;
import livr.LIVRUtils;

import static livr.api.Constant.CANNOT_BE_EMPTY;
import static livr.api.Constant.EMPTY;
import static livr.api.Constant.FORMAT_ERROR;
import static livr.api.Constant.REQUIRED;

/**
 * @author vladislavbaluk (creator)
 * @author Gábor KOLÁROVICS
 * 
 * @since 2017/09/28
 */
public final class CommonRules {

	private CommonRules() {
		throw new IllegalStateException("Utility class");
	}

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> required = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue())) {
			return REQUIRED;
		}
		return EMPTY;
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> not_empty = objects -> wrapper -> {
		if (wrapper.getValue() != null && wrapper.getValue().equals(EMPTY)) {
			return CANNOT_BE_EMPTY;
		}
		return EMPTY;
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> not_empty_list = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()))
			return CANNOT_BE_EMPTY;
		if (!(wrapper.getValue() instanceof JSONArray))
			return FORMAT_ERROR;
		if (((JSONArray) wrapper.getValue()).isEmpty())
			return CANNOT_BE_EMPTY;

		return EMPTY;
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> any_object = objects -> wrapper -> {
		if (LIVRUtils.isNoValue((wrapper.getValue())))
			return EMPTY;

		if (!LIVRUtils.isObject(wrapper.getValue())) {
			return FORMAT_ERROR;
		}
		return EMPTY;
	};

}
