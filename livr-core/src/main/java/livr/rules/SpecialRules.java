package livr.rules;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import livr.FunctionKeeper;
import livr.LIVRUtils;

import static livr.api.Constant.EMPTY;
import static livr.api.Constant.FIELDS_NOT_EQUAL;
import static livr.api.Constant.FORMAT_ERROR;
import static livr.api.Constant.WRONG_DATE;
import static livr.api.Constant.WRONG_EMAIL;
import static livr.api.Constant.WRONG_URL;

/**
 * @author vladislavbaluk (creator)
 * @author Gábor KOLÁROVICS
 * 
 * @since 2017/09/28
 */
public class SpecialRules {

	private SpecialRules() {
		throw new IllegalStateException("Utility class");
	}

	static final Pattern URL_PATTERN = Pattern.compile(
			"^(?:(?:http|https)://)(?:\\S+(?::\\S*)?@)?(?:(?:(?:[1-9]\\d?|1\\d\\d|2[0-1]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[0-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,})))\\.?|localhost)(?::\\d{2,5})?(?:[/?#]\\S*)?$",
			Pattern.CASE_INSENSITIVE);

	static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
			Pattern.CASE_INSENSITIVE);

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> email = objects -> {

		return wrapper -> {
			if (LIVRUtils.isNoValue(wrapper.getValue()))
				return EMPTY;
			if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
				return FORMAT_ERROR;

			String value = wrapper.getValue() + EMPTY;

			if (!VALID_EMAIL_ADDRESS_REGEX.matcher(value).matches())
				return WRONG_EMAIL;

			wrapper.getFieldResultArr().add(value);
			return EMPTY;
		};
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> equal_to_field = objects -> {
		final String field = objects.get(0) + EMPTY;
		return wrapper -> {
			if (LIVRUtils.isNoValue(wrapper.getValue()))
				return EMPTY;
			if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
				return FORMAT_ERROR;
			String value = wrapper.getValue() + EMPTY;

			if (!value.equals(wrapper.getArgs().get(field)))
				return FIELDS_NOT_EQUAL;
			return EMPTY;
		};
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> url = objects -> {

		return wrapper -> {
			if (LIVRUtils.isNoValue(wrapper.getValue()))
				return EMPTY;
			if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
				return FORMAT_ERROR;
			String value = wrapper.getValue() + EMPTY;

			if (value.length() < 2083 && URL_PATTERN.matcher(value).matches())
				return EMPTY;
			return WRONG_URL;
		};
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> iso_date = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()))
			return EMPTY;
		if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
			return FORMAT_ERROR;
		String value = wrapper.getValue() + EMPTY;

		if (value.matches(
				"(^(19|[2-9][0-9])\\d\\d-(((0[1-9]|1[012])-(0[1-9]|1[0-9]|2[0-8]))|((0[13578]|1[02])-(29|30|31))|((0[469]|11)-(29|30)))$)|(^(19|[2-9][0-9])(00|04|08|12|16|20|24|28|32|36|40|44|48|52|56|60|64|68|72|76|80|84|88|92|96)-02-29$)")) {
			return EMPTY;
		}
		return WRONG_DATE;

	};
}
