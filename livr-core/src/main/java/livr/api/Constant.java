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
package livr.api;

/**
 * LIVR default response errors code
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/11/07
 */
public final class Constant {

	// error
	public static final String FORMAT_ERROR = "FORMAT_ERROR";

	public static final String REQUIRED = "REQUIRED";
	public static final String CANNOT_BE_EMPTY = "CANNOT_BE_EMPTY";
	public static final String FIELDS_NOT_EQUAL = "FIELDS_NOT_EQUAL";
	public static final String NOT_INTEGER = "NOT_INTEGER";
	public static final String NOT_NUMBER = "NOT_NUMBER";
	public static final String NOT_DECIMAL = "NOT_DECIMAL";
	public static final String NOT_POSITIVE_INTEGER = "NOT_POSITIVE_INTEGER";
	public static final String NOT_POSITIVE_DECIMAL = "NOT_POSITIVE_DECIMAL";
	public static final String NOT_ALLOWED_VALUE = "NOT_ALLOWED_VALUE";
	public static final String WRONG_EMAIL = "WRONG_EMAIL";
	public static final String WRONG_URL = "WRONG_URL";
	public static final String WRONG_DATE = "WRONG_DATE";
	public static final String WRONG_FORMAT = "WRONG_FORMAT";
	public static final String TOO_LOW = "TOO_LOW";
	public static final String TOO_HIGH = "TOO_HIGH";
	public static final String TOO_SHORT = "TOO_SHORT";
	public static final String TOO_LONG = "TOO_LONG";
	// common
	public static final String EMPTY = "";

	public static final String FIELD = "field";

	private Constant() {
		throw new IllegalStateException("Utility class");
	}

}
