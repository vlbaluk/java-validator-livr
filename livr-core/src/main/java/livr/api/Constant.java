package livr.api;

/**
 * Constants
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/11/07
 */
public final class Constant {

	private Constant() {
		throw new IllegalStateException("Utility class");
	}

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

}
