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
            // json-simple reads a JSON number beyond the double range, like 1e400, as Infinity
            if (value instanceof Number && !Double.isFinite(((Number) value).doubleValue())) return "NOT_NUMBER";

            // A JSON number is counted in its string form, a string as it is written
            BigDecimal number = RuleArguments.number(value);
            if (number == null || Double.isInfinite(number.doubleValue())) return "NOT_NUMBER";
            if (number.stripTrailingZeros().scale() > maxDecimals) return "TOO_MANY_DECIMALS";

            Object output = value;
            if (value instanceof String) {
                // An integer that fits in a Long stays exact, anything else is a Double
                boolean isLong = number.scale() == 0 && number.toBigInteger().bitLength() < 64;
                output = isLong ? (Object) number.longValue() : (Object) number.doubleValue();
            }
            wrapper.getFieldResultArr().add(output);
            return "";
        };
    };
}
