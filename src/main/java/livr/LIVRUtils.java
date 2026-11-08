package livr;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * Created by vladislavbaluk on 9/28/2017.
 */
public class LIVRUtils {

    public static JSONParser newParser() {
        return new JSONParser();
    }

    public static boolean isPrimitiveValue(Object value) {
        if (value.getClass() == String.class || value.getClass() == Boolean.class) return true;
        if (value instanceof Number) return true;
        if (value.equals("true") || value.equals("false")) return true;
        return false;
    }

    public static boolean looksLikeNumber(Object value) {

        return value instanceof Number || NumberUtils.isCreatable(value + "");
    }

    public static boolean isObject(Object value) {
        return value instanceof JSONObject;
    }

    public static boolean isEmptyObject(Map<String, String> map) {
        return map.isEmpty();
    }

    public static boolean isNoValue(Object value) {
        return value == null || (value + "").equals("");
    }

    public static boolean isInteger(Object value) {
        if (value instanceof Long) {
            return true;
        }

        return (value + "").matches("-?\\d+");
    }

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

    /**
     * An empty value: a missing field, null or "" (LIVR 2.5, 2.1)
     */
    public static boolean isEmpty(Object value) {
        return value == null || "".equals(value);
    }

    /**
     * A string, a number or a boolean (LIVR 2.5, 2.2)
     */
    public static boolean isPrimitive(Object value) {
        return value instanceof String || value instanceof Number || value instanceof Boolean;
    }

    /**
     * The string form of a primitive value (LIVR 2.5, 2.2): a string as it is, "true" or "false"
     * for a boolean, a number as in JavaScript String(number): 1.0 is "1", 1e7 is "10000000"
     * (Double.toString gives "1.0" and "1.0E7")
     */
    public static String stringForm(Object value) {
        if (value instanceof String) return (String) value;
        if (value instanceof Long || value instanceof Integer || value instanceof BigInteger
                || value instanceof Short || value instanceof Byte) return value.toString();
        if (value instanceof Number && !Double.isFinite(((Number) value).doubleValue())) return value.toString();
        if (value instanceof Number) {
            // Double.toString and Float.toString give the shortest decimal that reads back as
            // the same number, NumberUtils.createNumber("1.5") in the numeric rules gives a Float
            BigDecimal number = value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString());
            return number.signum() == 0 ? "0" : number.stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value);
    }
}
