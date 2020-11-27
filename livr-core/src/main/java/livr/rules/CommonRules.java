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

import static livr.api.Constant.CANNOT_BE_EMPTY;
import static livr.api.Constant.EMPTY;
import static livr.api.Constant.FORMAT_ERROR;
import static livr.api.Constant.REQUIRED;

import java.util.List;
import java.util.function.Function;

import org.json.simple.JSONArray;

import livr.FunctionKeeper;
import livr.LIVRUtils;

/**
 * @author vladislavbaluk
 * @author Gábor KOLÁROVICS
 * 
 * @since 2017/09/28
 */
public final class CommonRules {

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

	private CommonRules() {
		throw new IllegalStateException("Utility class");
	}

}
