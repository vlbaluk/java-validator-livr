package livr.Rules;

import livr.FunctionKeeper;
import livr.LIVRUtils;
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
}
