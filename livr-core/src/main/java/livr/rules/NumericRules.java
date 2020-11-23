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

import static livr.api.Constant.EMPTY;
import static livr.api.Constant.FORMAT_ERROR;
import static livr.api.Constant.NOT_DECIMAL;
import static livr.api.Constant.NOT_INTEGER;
import static livr.api.Constant.NOT_NUMBER;
import static livr.api.Constant.NOT_POSITIVE_DECIMAL;
import static livr.api.Constant.NOT_POSITIVE_INTEGER;
import static livr.api.Constant.TOO_HIGH;
import static livr.api.Constant.TOO_LOW;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONArray;

import livr.FunctionKeeper;
import livr.LIVRUtils;

/**
 * @author vladislavbaluk
 * @author Gábor KOLÁROVICS
 * 
 * @since 2017/09/28
 */
public class NumericRules {

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

    private NumericRules() {
	throw new IllegalStateException("Utility class");
    }
}
