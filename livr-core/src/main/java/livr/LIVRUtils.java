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
package livr;

import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * @author vladislavbaluk
 * @author Gábor KOLÁROVICS
 * 
 * @since 2017/09/28
 */
public class LIVRUtils {

	public static boolean isDecimal(Object value) {
		if (value instanceof Double) {
			return true;
		}

		try {
			Double.valueOf(value + "");
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static boolean isEmptyObject(Map<String, String> map) {
		return map.isEmpty();
	}

	public static boolean isInteger(Object value) {
		if (value instanceof Long) {
			return true;
		}

		return NumberUtils.isDigits(value + "");
	}

	public static boolean isNoValue(Object value) {
		return value == null || (value + "").equals("");
	}

	public static boolean isObject(Object value) {
		return value.getClass() == JSONObject.class;
	}

	public static boolean isPrimitiveValue(Object value) {
		if (value.getClass() == String.class || value.getClass() == Boolean.class)
			return true;
		if (value instanceof Number)
			return true;
		if (value.equals("true") || value.equals("false"))
			return true;
		return false;
	}

	public static boolean looksLikeNumber(Object value) {

		return value instanceof Number || NumberUtils.isCreatable(value + "");
	}

	public static JSONParser newParser() {
		return new JSONParser();
	}
}
