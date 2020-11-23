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
import static livr.api.Constant.NOT_ALLOWED_VALUE;
import static livr.api.Constant.TOO_LONG;
import static livr.api.Constant.TOO_SHORT;
import static livr.api.Constant.WRONG_FORMAT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;

import livr.FunctionKeeper;
import livr.LIVRUtils;

/**
 * @author vladislavbaluk
 * @author Gábor KOLÁROVICS
 * 
 * @since 2017/09/28
 */
public class StringRules {

    public static final Function<List<Object>, Function<FunctionKeeper, Object>> string = objects -> wrapper -> {
	if (wrapper.getValue() == null || (wrapper.getValue() + EMPTY).equals(EMPTY))
	    return EMPTY;
	if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
	    return FORMAT_ERROR;

	String value = wrapper.getValue() + EMPTY;
	wrapper.getFieldResultArr().add(value);
	return EMPTY;
    };

    public static final Function<List<Object>, Function<FunctionKeeper, Object>> eq = objects -> {
	Object allowedValue = objects.get(0);

	return wrapper -> {
	    if (wrapper.getValue() == null || (wrapper.getValue() + EMPTY).equals(EMPTY))
		return EMPTY;
	    if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
		return FORMAT_ERROR;

	    String value = wrapper.getValue() + EMPTY;
	    if (value.equals(allowedValue + EMPTY)) {
		wrapper.getFieldResultArr().add(allowedValue);
		return EMPTY;
	    }

	    return NOT_ALLOWED_VALUE;
	};
    };

    public static final Function<List<Object>, Function<FunctionKeeper, Object>> one_of = objects -> {
	Object[] objects1 = ((JSONArray) objects.get(0)).toArray();
	final List<Object> allowedValues = new ArrayList();
	Collections.addAll(allowedValues, objects1);

	return wrapper -> {
	    List<String> allowedStrValues = allowedValues.stream().map(obj -> String.valueOf(obj))
		    .collect(Collectors.toList());
	    if (wrapper.getValue() == null || (wrapper.getValue() + EMPTY).equals(EMPTY))
		return EMPTY;
	    if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
		return FORMAT_ERROR;

	    String value = wrapper.getValue() + EMPTY;
	    if (allowedStrValues.contains(value)) {
		wrapper.getFieldResultArr().add(allowedValues.get(allowedStrValues.indexOf(value)));
		return EMPTY;
	    }

	    return NOT_ALLOWED_VALUE;
	};
    };

    public static final Function<List<Object>, Function<FunctionKeeper, Object>> max_length = objects -> {
	final Long maxLength = Long.valueOf(objects.get(0) + EMPTY);

	return wrapper -> {
	    if (wrapper.getValue() == null || (wrapper.getValue() + EMPTY).equals(EMPTY))
		return EMPTY;
	    if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
		return FORMAT_ERROR;

	    String value = wrapper.getValue() + EMPTY;
	    if (value.length() > maxLength)
		return TOO_LONG;
	    wrapper.getFieldResultArr().add(value);
	    return EMPTY;
	};
    };

    public static final Function<List<Object>, Function<FunctionKeeper, Object>> min_length = objects -> {
	final Long minLength = Long.valueOf(objects.get(0) + EMPTY);

	return wrapper -> {
	    if (wrapper.getValue() == null || (wrapper.getValue() + EMPTY).equals(EMPTY))
		return EMPTY;
	    if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
		return FORMAT_ERROR;

	    String value = wrapper.getValue() + EMPTY;
	    if (value.length() < minLength)
		return TOO_SHORT;
	    wrapper.getFieldResultArr().add(value);
	    return EMPTY;
	};
    };

    public static final Function<List<Object>, Function<FunctionKeeper, Object>> length_equal = objects -> {
	final Long length = Long.valueOf(objects.get(0) + EMPTY);

	return wrapper -> {
	    if (wrapper.getValue() == null || (wrapper.getValue() + EMPTY).equals(EMPTY))
		return EMPTY;
	    if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
		return FORMAT_ERROR;

	    String value = wrapper.getValue() + EMPTY;

	    if (value.length() < length)
		return TOO_SHORT;
	    if (value.length() > length)
		return TOO_LONG;

	    wrapper.getFieldResultArr().add(value);
	    return EMPTY;
	};
    };

    public static final Function<List<Object>, Function<FunctionKeeper, Object>> length_between = objects -> {
	Iterator it = ((JSONArray) objects.get(0)).iterator();
	final Long minLength = Long.valueOf(it.next() + EMPTY);
	final Long maxLength = Long.valueOf(it.next() + EMPTY);

	return wrapper -> {
	    if (wrapper.getValue() == null || (wrapper.getValue() + EMPTY).equals(EMPTY))
		return EMPTY;
	    if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
		return FORMAT_ERROR;

	    String value = wrapper.getValue() + EMPTY;
	    if (value.length() < minLength)
		return TOO_SHORT;
	    if (value.length() > maxLength)
		return TOO_LONG;

	    wrapper.getFieldResultArr().add(value);
	    return EMPTY;
	};
    };

    public static final Function<List<Object>, Function<FunctionKeeper, Object>> like = objects -> {
	String pattern;
	boolean isIgnoreCase;
	if (objects.get(0).getClass() == JSONArray.class) {
	    Iterator it = ((JSONArray) objects.get(0)).iterator();
	    pattern = (String) it.next();
	    isIgnoreCase = it.next().equals("i");
	} else {
	    pattern = (String) objects.get(0);
	    isIgnoreCase = false;
	}

	return wrapper -> {
	    if (wrapper.getValue() == null || (wrapper.getValue() + EMPTY).equals(EMPTY))
		return EMPTY;
	    if (!LIVRUtils.isPrimitiveValue(wrapper.getValue()))
		return FORMAT_ERROR;

	    String value = wrapper.getValue() + EMPTY;
	    String caseInValue = isIgnoreCase ? value.toLowerCase() : value;
	    if (!caseInValue.matches(pattern))
		return WRONG_FORMAT;
	    wrapper.getFieldResultArr().add(value);
	    return EMPTY;
	};
    };

    private StringRules() {
	throw new IllegalStateException("Utility class");
    }

}
