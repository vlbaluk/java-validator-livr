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

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import livr.FunctionKeeper;
import livr.LIVRUtils;

/**
 * @author vladislavbaluk
 * @author Gábor KOLÁROVICS
 * 
 * @since 2017/09/28
 */
public class Modifiers {

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

    private Modifiers() {
	throw new IllegalStateException("Utility class");
    }
}
