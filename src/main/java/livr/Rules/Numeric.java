package livr.Rules;

import livr.FunctionKeeper;
import livr.LIVRUtils;
import livr.RuleArguments;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONArray;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Created by vladislavbaluk on 9/28/2017.
 */
public class Numeric {
    public static Function<List<Object>, Function> integer = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
        if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";
        if (!LIVRUtils.looksLikeNumber(wrapper.getValue())) return "NOT_INTEGER";

        if (!LIVRUtils.isInteger(wrapper.getValue())) return "NOT_INTEGER";
        wrapper.getFieldResultArr().add(NumberUtils.createNumber(wrapper.getValue() + ""));
        return "";
    };

    public static Function<List<Object>, Function> positive_integer = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
        if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";
        if (!LIVRUtils.looksLikeNumber(wrapper.getValue())) return "NOT_POSITIVE_INTEGER";

        if (!LIVRUtils.isInteger(wrapper.getValue()) || new BigDecimal(wrapper.getValue() + "").signum() < 1)
            return "NOT_POSITIVE_INTEGER";
        wrapper.getFieldResultArr().add(NumberUtils.createNumber(wrapper.getValue() + ""));
        return "";
    };

    public static Function<List<Object>, Function> decimal = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
        if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";
        if (!LIVRUtils.looksLikeNumber(wrapper.getValue())) return "NOT_DECIMAL";

        if (!LIVRUtils.isDecimal(wrapper.getValue())) return "NOT_DECIMAL";
        wrapper.getFieldResultArr().add(NumberUtils.createNumber(wrapper.getValue() + ""));
        return "";
    };

    public static Function<List<Object>, Function> positive_decimal = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
        if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";
        if (!LIVRUtils.looksLikeNumber(wrapper.getValue())) return "NOT_POSITIVE_DECIMAL";

        if (!LIVRUtils.isDecimal(wrapper.getValue()) || Double.valueOf(wrapper.getValue() + "") <= 0)
            return "NOT_POSITIVE_DECIMAL";
        wrapper.getFieldResultArr().add(NumberUtils.createNumber(wrapper.getValue() + ""));
        return "";
    };

    public static Function<List<Object>, Function> max_number = objects -> {
        final Double maxNumber = Double.valueOf(objects.get(0) + "");
        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";
            if (!LIVRUtils.looksLikeNumber(wrapper.getValue())) return "NOT_NUMBER";

            Double value = Double.valueOf(wrapper.getValue() + "");

            if (value > maxNumber) return "TOO_HIGH";

            wrapper.getFieldResultArr().add(NumberUtils.createNumber(wrapper.getValue() + ""));
            return "";
        };
    };

    public static Function<List<Object>, Function> min_number = objects -> {
        final Double min_number = Double.valueOf(objects.get(0) + "");
        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";
            if (!LIVRUtils.looksLikeNumber(wrapper.getValue())) return "NOT_NUMBER";
            Double value = Double.valueOf(wrapper.getValue() + "");

            if (value < min_number) return "TOO_LOW";

            wrapper.getFieldResultArr().add(NumberUtils.createNumber(wrapper.getValue() + ""));
            return "";
        };
    };

    public static Function<List<Object>, Function> number_between = objects -> {
        Iterator it = ((JSONArray) objects.get(0)).iterator();
        final BigDecimal minNumber = new BigDecimal(it.next() + "");
        final BigDecimal maxNumber = new BigDecimal(it.next() + "");

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (LIVRUtils.isNoValue(wrapper.getValue())) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";
            if (!LIVRUtils.looksLikeNumber(wrapper.getValue())) return "NOT_NUMBER";
            BigDecimal value = NumberUtils.createBigDecimal(wrapper.getValue() + "");
            if (value.compareTo(minNumber) < 0) return "TOO_LOW";
            if (value.compareTo(maxNumber) > 0) return "TOO_HIGH";

            wrapper.getFieldResultArr().add(NumberUtils.createNumber(wrapper.getValue() + ""));
            return "";
        };
    };

    private static final Pattern DECIMAL = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");

    /**
     * At most that many digits after the decimal point, trailing zeros do not count. A string
     * in the grammar of "decimal" or a JSON number; the output is a number (LIVR 2.5, 2.4)
     */
    public static Function<List<Object>, Function> max_decimals = objects -> {
        List<Object> args = RuleArguments.get(objects);
        if (args.isEmpty()) throw RuleArguments.schemaError("\"max_decimals\" needs a number of digits");
        final int maxDecimals = RuleArguments.nonNegativeInteger(args.get(0), "max_decimals");

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            Object value = wrapper.getValue();
            if (LIVRUtils.isEmpty(value)) return "";
            if (!LIVRUtils.isPrimitive(value)) return "FORMAT_ERROR";
            if (value instanceof Boolean) return "NOT_NUMBER";

            Object number = value;
            if (value instanceof String) {
                if (!DECIMAL.matcher((String) value).matches()) return "NOT_NUMBER";
                double parsed = Double.parseDouble((String) value);
                if (Double.isInfinite(parsed)) return "NOT_NUMBER";
                number = ((String) value).indexOf('.') < 0 && ((String) value).length() < 19
                        ? (Object) Long.valueOf((String) value)
                        : (Object) parsed;
            }

            // A JSON number is counted in its string form, a string as it is written
            String digits = value instanceof String ? (String) value : LIVRUtils.stringForm(value);
            int point = digits.indexOf('.');
            int decimals = 0;
            if (point >= 0) {
                int end = digits.length();
                while (end > point + 1 && digits.charAt(end - 1) == '0') end--;
                decimals = end - point - 1;
            }
            if (decimals > maxDecimals) return "TOO_MANY_DECIMALS";

            wrapper.getFieldResultArr().add(number);
            return "";
        };
    };
}
