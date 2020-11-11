package livr.rules;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONArray;

import livr.FunctionKeeper;
import livr.LIVRUtils;

import static livr.api.Constant.EMPTY;
import static livr.api.Constant.FORMAT_ERROR;
import static livr.api.Constant.NOT_DECIMAL;
import static livr.api.Constant.NOT_INTEGER;
import static livr.api.Constant.NOT_NUMBER;
import static livr.api.Constant.NOT_POSITIVE_DECIMAL;
import static livr.api.Constant.NOT_POSITIVE_INTEGER;
import static livr.api.Constant.TOO_HIGH;
import static livr.api.Constant.TOO_LOW;

/**
 * @author vladislavbaluk (creator)
 * @author Gábor KOLÁROVICS
 * 
 * @since 2017/09/28
 */
public class NumericRules {

	private NumericRules() {
		throw new IllegalStateException("Utility class");
	}

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> integer = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()))
			return EMPTY;
		if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
			return FORMAT_ERROR;
		if (!LIVRUtils.looksLikeNumber(wrapper.getValue()))
			return NOT_INTEGER;

		if (!LIVRUtils.isInteger(wrapper.getValue()))
			return NOT_INTEGER;
		wrapper.getFieldResultArr().add(NumberUtils.createNumber(wrapper.getValue() + EMPTY));
		return EMPTY;
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> positive_integer = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()))
			return EMPTY;
		if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
			return FORMAT_ERROR;
		if (!LIVRUtils.looksLikeNumber(wrapper.getValue()))
			return NOT_POSITIVE_INTEGER;

		if (!LIVRUtils.isInteger(wrapper.getValue()) || Long.valueOf(wrapper.getValue() + EMPTY) < 1)
			return NOT_POSITIVE_INTEGER;
		wrapper.getFieldResultArr().add(NumberUtils.createNumber(wrapper.getValue() + EMPTY));
		return EMPTY;
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> decimal = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()))
			return EMPTY;
		if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
			return FORMAT_ERROR;
		if (!LIVRUtils.looksLikeNumber(wrapper.getValue()))
			return NOT_DECIMAL;

		if (!LIVRUtils.isDecimal(wrapper.getValue()))
			return NOT_DECIMAL;
		wrapper.getFieldResultArr().add(NumberUtils.createNumber(wrapper.getValue() + EMPTY));
		return EMPTY;
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> positive_decimal = objects -> wrapper -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()))
			return EMPTY;
		if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
			return FORMAT_ERROR;
		if (!LIVRUtils.looksLikeNumber(wrapper.getValue()))
			return NOT_POSITIVE_DECIMAL;

		if (!LIVRUtils.isDecimal(wrapper.getValue()) || Double.valueOf(wrapper.getValue() + EMPTY) <= 0)
			return NOT_POSITIVE_DECIMAL;
		wrapper.getFieldResultArr().add(NumberUtils.createNumber(wrapper.getValue() + EMPTY));
		return EMPTY;
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> max_number = objects -> {
		final Long maxNumber = Long.valueOf(objects.get(0) + EMPTY);
		return wrapper -> {
			if (LIVRUtils.isNoValue(wrapper.getValue()))
				return EMPTY;
			if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
				return FORMAT_ERROR;
			if (!LIVRUtils.looksLikeNumber(wrapper.getValue()))
				return NOT_NUMBER;

			Double value = Double.valueOf(wrapper.getValue() + EMPTY);

			if (value > maxNumber)
				return TOO_HIGH;

			wrapper.getFieldResultArr().add(NumberUtils.createNumber(wrapper.getValue() + EMPTY));
			return EMPTY;
		};
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> min_number = objects -> {
		final Long minNumber = Long.valueOf(objects.get(0) + EMPTY);
		return wrapper -> {
			if (LIVRUtils.isNoValue(wrapper.getValue()))
				return EMPTY;
			if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
				return FORMAT_ERROR;
			if (!LIVRUtils.looksLikeNumber(wrapper.getValue()))
				return NOT_NUMBER;
			Double value = Double.valueOf(wrapper.getValue() + EMPTY);

			if (value < minNumber)
				return TOO_LOW;

			wrapper.getFieldResultArr().add(NumberUtils.createNumber(wrapper.getValue() + EMPTY));
			return EMPTY;
		};
	};

	public static final Function<List<Object>, Function<FunctionKeeper, Object>> number_between = objects -> {
		Iterator it = ((JSONArray) objects.get(0)).iterator();
		final Long minNumber = Long.valueOf(it.next() + EMPTY);
		final Long maxNumber = Long.valueOf(it.next() + EMPTY);

		return wrapper -> {
			if (LIVRUtils.isNoValue(wrapper.getValue()))
				return EMPTY;
			if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
				return FORMAT_ERROR;
			if (!LIVRUtils.looksLikeNumber(wrapper.getValue()))
				return NOT_NUMBER;
			BigDecimal value = NumberUtils.createBigDecimal(wrapper.getValue() + EMPTY);
			if (value.compareTo(BigDecimal.valueOf(minNumber)) < 0)
				return TOO_LOW;
			if (value.compareTo(BigDecimal.valueOf(maxNumber)) > 0)
				return TOO_HIGH;

			wrapper.getFieldResultArr().add(NumberUtils.createNumber(wrapper.getValue() + EMPTY));
			return EMPTY;
		};
	};
}
