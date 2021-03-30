/*
 * Copyright (C) 2020 Gábor KOLÁROVICS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package livr.rules;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;
import static livr.api.Constant.EMPTY;
import static livr.api.Constant.FORMAT_ERROR;
import static livr.api.Constant.REQUIRED;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import livr.FunctionKeeper;
import livr.LIVRUtils;

/**
 * Java implementation of JavaScript extra rules
 *
 * https://livr-spec.org/introduction/extensions.html
 *
 * @author Gábor KOLÁROVICS
 */
public class ExtraRules {

	static final Pattern IP_PATTERN = Pattern.compile(
			"^(25[0-5]|2[0-4][0-9]|[1]?[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|[1]?[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|[1]?[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|[1]?[1-9]?[0-9])$");

	static final Pattern UUIDV1_PATTERN = Pattern
			.compile("^[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}$", Pattern.CASE_INSENSITIVE);

	static final Pattern UUIDV2_PATTERN = Pattern
			.compile("^[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}$", Pattern.CASE_INSENSITIVE);

	static final Pattern UUIDV3_PATTERN = Pattern
			.compile("^[0-9A-F]{8}-[0-9A-F]{4}-3[0-9A-F]{3}-[0-9A-F]{4}-[0-9A-F]{12}$", Pattern.CASE_INSENSITIVE);

	static final Pattern UUIDV4_PATTERN = Pattern
			.compile("^[0-9A-F]{8}-[0-9A-F]{4}-4[0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}$", Pattern.CASE_INSENSITIVE);

	static final Pattern UUIDV5_PATTERN = Pattern
			.compile("^[0-9A-F]{8}-[0-9A-F]{4}-5[0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}$", Pattern.CASE_INSENSITIVE);

	static final Pattern BASE64REQ_PATTERN = Pattern
			.compile("^(?:[A-Za-z0-9+\\/]{4})*(?:[A-Za-z0-9+\\/]{2}==|[A-Za-z0-9+\\/]{3}=)?$");

	static final Pattern BASE64OPT_PATTERN = Pattern
			.compile("^(?:[A-Za-z0-9+\\/]{4})*(?:[A-Za-z0-9+\\/]{2}(==)?|[A-Za-z0-9+\\/]{3}=?)?$");

	static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d*$");

	static final Pattern MD5_PATTERN = Pattern.compile("^[a-f0-9]{32}$", Pattern.CASE_INSENSITIVE);

	static final Pattern MONGOID_PATTERN = Pattern.compile("^[0-9a-fA-F]{24}$");

	static final Pattern ISO_DATE_PATTERN = Pattern.compile(
			"^(([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9]))(T(2[0-3]|[01][0-9]):([0-5][0-9])(:([0-5][0-9])(\\.[0-9]+)?)?(Z|[\\+\\-](2[0-3]|[01][0-9]):([0-5][0-9])))?$");

	static final Pattern DATE = Pattern.compile("^(\\d{4})-([0-1][0-9])-([0-3][0-9])$");

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> ipv4 = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()))
			return EMPTY;
		if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
			return FORMAT_ERROR;
		String value = wrapper.getValue() + EMPTY;

		if (IP_PATTERN.matcher(value).matches())
			return EMPTY;
		return "NOT_IP";
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> uuid = objects -> {
		final String field = objects.get(0) + EMPTY;

		return wrapper -> {
			if (LIVRUtils.isNoValue(wrapper.getValue()))
				return EMPTY;
			if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
				return FORMAT_ERROR;
			String value = wrapper.getValue() + EMPTY;

			switch (field) {
			case "v1":
				if (UUIDV1_PATTERN.matcher(value).matches())
					return EMPTY;
				break;
			case "v2":
				if (UUIDV2_PATTERN.matcher(value).matches())
					return EMPTY;
				break;
			case "v3":
				if (UUIDV3_PATTERN.matcher(value).matches())
					return EMPTY;
				break;
			case "v5":
				if (UUIDV5_PATTERN.matcher(value).matches())
					return EMPTY;
				break;
			default:
				if (UUIDV4_PATTERN.matcher(value).matches())
					return EMPTY;
				break;
			}

			return "NOT_UUID";
		};
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> boolean_rule = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()))
			return EMPTY;
		if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
			return FORMAT_ERROR;
		String value = wrapper.getValue() + EMPTY;

		if ("1".equals(value) || "true".equalsIgnoreCase(value)) {
			wrapper.getFieldResultArr().add(true);
			return EMPTY;
		} else if ("0".equals(value) || "false".equalsIgnoreCase(value)) {
			wrapper.getFieldResultArr().add(false);
			return EMPTY;
		}
		return "NOT_BOOLEAN";
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> base64 = objects -> {
		final String padding = objects.get(0) + EMPTY;

		return wrapper -> {
			if (LIVRUtils.isNoValue(wrapper.getValue()))
				return EMPTY;
			if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
				return FORMAT_ERROR;
			String value = wrapper.getValue() + EMPTY;

			if ("relaxed".equals(padding)) {
				if (BASE64OPT_PATTERN.matcher(value).matches()) {
					wrapper.getFieldResultArr().add((wrapper.getValue() + "").trim());
					return EMPTY;
				}
			} else {
				if (BASE64REQ_PATTERN.matcher(value).matches()) {
					wrapper.getFieldResultArr().add((wrapper.getValue() + "").trim());
					return EMPTY;
				}
			}

			return "MALFORMED_BASE64";
		};
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> credit_card = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()))
			return EMPTY;
		if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
			return FORMAT_ERROR;
		String value = wrapper.getValue() + EMPTY;

		if ((value.length() > 16 || value.length() < 14) && !NUMBER_PATTERN.matcher(value).matches())
			return "WRONG_CREDIT_CARD_NUMBER";

		int n = value.length();
		int sum = 0;

		for (int i = 0; i < n; i++) {
			int v = Character.getNumericValue(value.charAt(i));
			if (i % 2 == 0) {
				v *= 2;
			}
			if (v > 9) {
				v -= 9;
			}
			sum += v;
		}

		if (sum % 10 != 0)
			return "WRONG_CREDIT_CARD_NUMBER";

		return EMPTY;
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> is = objects -> {
		final String field = objects.get(0) + EMPTY;

		return wrapper -> {

			if (LIVRUtils.isNoValue(wrapper.getValue()))
				return REQUIRED;
			if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
				return FORMAT_ERROR;
			String value = wrapper.getValue() + EMPTY;

			if (field.equals(value)) {
				wrapper.getFieldResultArr().add(objects.get(0));
				return EMPTY;
			}

			return "NOT_ALLOWED_VALUE";
		};
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> list_length = objects -> {
		Long minVal = 0L;
		Long maxVal = 0L;
		if (objects.get(0) instanceof JSONArray) {
			Iterator<?> it = ((JSONArray) objects.get(0)).iterator();
			minVal = Long.valueOf(it.next() + EMPTY);
			maxVal = it.hasNext() ? Long.valueOf(it.next() + EMPTY) : minVal;
		} else {
			minVal = Long.valueOf(objects.get(0) + EMPTY);
			maxVal = minVal;
		}
		final Long minLength = minVal;
		final Long maxLength = maxVal;

		return wrapper -> {

			if (LIVRUtils.isNoValue(wrapper.getValue()))
				return EMPTY;
			if (!(wrapper.getValue() instanceof JSONArray))
				return FORMAT_ERROR;

			Object[] arr = ((JSONArray) wrapper.getValue()).toArray();

			if (arr.length < minLength)
				return "TOO_FEW_ITEMS";
			if (arr.length > maxLength)
				return "TOO_MANY_ITEMS";

			return EMPTY;
		};
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> md5 = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()))
			return EMPTY;
		if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
			return FORMAT_ERROR;
		String value = wrapper.getValue() + EMPTY;

		if (!MD5_PATTERN.matcher(value).matches())
			return "NOT_MD5";
		return EMPTY;
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> mongo_id = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()))
			return EMPTY;
		if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
			return FORMAT_ERROR;
		String value = wrapper.getValue() + EMPTY;

		if (!MONGOID_PATTERN.matcher(value).matches())
			return "NOT_ID";
		return EMPTY;
	};

	@SuppressWarnings("unchecked")
	public static final Function<List<Object>, Function<FunctionKeeper, Object>> required_if = objects -> {
		String f = EMPTY;
		Object v = EMPTY;
		if (objects.get(0) instanceof JSONObject) {
			final Map<String, Object> args = (Map<String, Object>) objects.get(0);
			Entry<String, Object> e = args.entrySet().iterator().next();
			f = e.getKey();
			v = e.getValue();
		}
		final String field = f;
		final Object expected = v;

		return wrapper -> {

			if (!LIVRUtils.isNoValue(wrapper.getValue()) || EMPTY.equals(expected))
				return EMPTY;

			Iterator<String> path = Arrays.asList(field.split("/")).iterator();
			Object current = jsonWalker(path, wrapper.getArgs().get(path.next()));

			if (expected.equals(current) && LIVRUtils.isNoValue(wrapper.getValue()))
				return REQUIRED;
			return EMPTY;
		};
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> list_items_unique = objects -> {
		return wrapper -> {
			if (LIVRUtils.isNoValue(wrapper.getValue()))
				return EMPTY;
			if (!(wrapper.getValue() instanceof JSONArray))
				return FORMAT_ERROR;

			Set<Object> collection = new HashSet<>();
			boolean unique = true;
			for (Object o : (JSONArray) wrapper.getValue()) {
				if (!LIVRUtils.isPrimitiveValue(o))
					return "INCOMPARABLE_ITEMS";
				if (collection.contains(o)) {
					unique = false;
				} else {
					collection.add(o);
				}
			}
			if (!unique)
				return "NOT_UNIQUE_ITEMS";
			return EMPTY;
		};
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> iso_date = objects -> {
		final String min = getStringParamValue(objects, "min");
		final String max = getStringParamValue(objects, "max");
		final String format = getStringParamValue(objects, "format", "date");

		return wrapper -> {
			if (LIVRUtils.isNoValue(wrapper.getValue())) {
				return EMPTY;
			}
			if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) {
				return FORMAT_ERROR;
			}

			String value = wrapper.getValue() + EMPTY;

			if (!ISO_DATE_PATTERN.matcher(value).matches()) {
				return "WRONG_DATE";
			}

			DateTimeFormatterBuilder b = new DateTimeFormatterBuilder().appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
					.appendLiteral('-').appendValue(MONTH_OF_YEAR, 2).appendLiteral('-').appendValue(DAY_OF_MONTH, 2);
			if ("datetime".equals(format)) {
				b.appendLiteral('T').appendValue(HOUR_OF_DAY, 2).appendLiteral(':').appendValue(MINUTE_OF_HOUR, 2)
						.optionalStart().appendLiteral(':').appendValue(SECOND_OF_MINUTE, 2).optionalStart()
						.appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true).appendLiteral("Z");
			}

			DateTimeFormatter df = b.toFormatter();

			try {
				ZonedDateTime epoch = dynamicZDateParser(value, false);
				if (min != null && epoch.compareTo(dynamicZDateParser(min, false)) < 0) {
					return "DATE_TOO_LOW";
				}
				if (max != null && epoch.compareTo(dynamicZDateParser(max, true)) > 0) {
					return "DATE_TOO_HIGH";
				}
				wrapper.getFieldResultArr().add(epoch.format(df));
			} catch (Exception e) {
				return "WRONG_DATE";
			}

			return EMPTY;
		};

	};

	@SuppressWarnings("unchecked")
	private static Object jsonWalker(final Iterator<String> path, final Object json) {
		if (path.hasNext()) {
			String s = path.next();
			if (json instanceof List && LIVRUtils.isInteger(s)) {
				Object v = ((ArrayList<Object>) json).get(Integer.valueOf(s));
				return jsonWalker(path, v);
			}
			if (json instanceof Map) {
				Object v = ((Map<String, Object>) json).get(s);
				return jsonWalker(path, v);
			}
		}
		return json;
	}

	private static String getStringParamValue(final List<Object> objects, final String key) {
		return getStringParamValue(objects, key, null);
	}

	@SuppressWarnings("unchecked")
	private static String getStringParamValue(final List<Object> objects, final String key, final String defValue) {
		if (objects.get(0) instanceof JSONObject) {
			final Map<String, Object> args = (Map<String, Object>) objects.get(0);
			Object value = args.get(key);
			if (value instanceof String) {
				return (String) value;
			}
		}
		return defValue;

	}

	private static ZonedDateTime dynamicZDateParser(final String value, final boolean isMax) {
		if ("tomorrow".equals(value)) {
			return ZonedDateTime.now().plusDays(1);
		} else if ("yesterday".equals(value)) {
			return ZonedDateTime.now().minusDays(1);
		} else if ("current".equals(value)) {
			return ZonedDateTime.now();
		}

		List<DateTimeFormatter> knownZonedPatterns = new ArrayList<>();
		knownZonedPatterns.add(DateTimeFormatter.ISO_DATE_TIME);
		knownZonedPatterns.add(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'"));
		knownZonedPatterns.add(DateTimeFormatter.ISO_DATE);
		for (DateTimeFormatter dateTimeFormatter : knownZonedPatterns) {
			try {
				return ZonedDateTime.parse(value, dateTimeFormatter).withZoneSameInstant(ZoneOffset.UTC);
			} catch (Exception e) {
				// NOOP
			}
		}

		for (DateTimeFormatter dateTimeFormatter : knownZonedPatterns) {
			try {
				LocalDate date = LocalDate.parse(value, dateTimeFormatter);
				ZonedDateTime zonedDate = date.atStartOfDay(ZoneId.systemDefault());
				if (isMax) {
					zonedDate = zonedDate.plusDays(1).minusNanos(1);
				}
				return zonedDate;
			} catch (Exception e) {
				// NOOP
			}
		}

		throw new DateTimeParseException("Unable to parse the date:", value, 0);

	}

	private ExtraRules() {
		throw new IllegalStateException("Utility class");
	}

}
