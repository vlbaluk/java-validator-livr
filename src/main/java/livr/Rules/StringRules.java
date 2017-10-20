package livr.Rules;

import com.google.common.collect.Lists;
import livr.FunctionKeeper;
import livr.LIVRUtils;
import org.json.simple.JSONArray;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by vladislavbaluk on 9/28/2017.
 */
public class StringRules {

    public static Function<List<Object>, Function> string = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (wrapper.getValue() == null || (wrapper.getValue() + "").equals("")) return "";
        if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";

        String value = wrapper.getValue() + "";
        wrapper.getFieldResultArr().add(value);
        return "";
    };

    public static Function<List<Object>, Function> eq = objects -> {
        Object allowedValue = objects.get(0);

        return (Function<FunctionKeeper, Object>) (wrapper) -> {
            if (wrapper.getValue() == null || (wrapper.getValue() + "").equals("")) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";

            String value = wrapper.getValue() + "";
            if (value.equals(allowedValue + "")) {
                wrapper.getFieldResultArr().add(allowedValue);
                return "";
            }

            return "NOT_ALLOWED_VALUE";
        };
    };


    public static Function<List<Object>, Function> one_of = objects -> {
        final List<Object> allowedValues = Lists.newArrayList(((JSONArray) objects.get(0)).toArray());

        return (Function<FunctionKeeper, Object>) (FunctionKeeper wrapper) -> {
            List<String> allowedStrValues = allowedValues.stream().map(obj -> String.valueOf(obj)).collect(Collectors.toList());
            if (wrapper.getValue() == null || (wrapper.getValue() + "").equals("")) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";

            String value = wrapper.getValue() + "";
            if (allowedStrValues.contains(value)) {
                wrapper.getFieldResultArr().add(allowedValues.get(allowedStrValues.indexOf(value)));
                return "";
            }

            return "NOT_ALLOWED_VALUE";
        };
    };

    public static Function<List<Object>, Function> max_length = objects -> {
        final Long maxLength = Long.valueOf(objects.get(0) + "");

        return (Function<FunctionKeeper, Object>) (FunctionKeeper wrapper) -> {
            if (wrapper.getValue() == null || (wrapper.getValue() + "").equals("")) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";

            String value = wrapper.getValue() + "";
            if (value.length() > maxLength) return "TOO_LONG";
            wrapper.getFieldResultArr().add(value);
            return "";
        };
    };

    public static Function<List<Object>, Function> min_length = objects -> {
        final Long minLength = Long.valueOf(objects.get(0) + "");

        return (Function<FunctionKeeper, Object>) (FunctionKeeper wrapper) -> {
            if (wrapper.getValue() == null || (wrapper.getValue() + "").equals("")) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";

            String value = wrapper.getValue() + "";
            if (value.length() < minLength) return "TOO_SHORT";
            wrapper.getFieldResultArr().add(value);
            return "";
        };
    };

    public static Function<List<Object>, Function> length_equal = objects -> {
        final Long length = Long.valueOf(objects.get(0) + "");

        return (Function<FunctionKeeper, Object>) (FunctionKeeper wrapper) -> {
            if (wrapper.getValue() == null || (wrapper.getValue() + "").equals("")) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";

            String value = wrapper.getValue() + "";

            if (value.length() < length) return "TOO_SHORT";
            if (value.length() > length) return "TOO_LONG";

            wrapper.getFieldResultArr().add(value);
            return "";
        };
    };

    public static Function<List<Object>, Function> length_between = objects -> {
        Iterator it = ((JSONArray) objects.get(0)).iterator();
        final Long minLength = Long.valueOf(it.next() + "");
        final Long maxLength = Long.valueOf(it.next() + "");

        return (Function<FunctionKeeper, Object>) (FunctionKeeper wrapper) -> {
            if (wrapper.getValue() == null || (wrapper.getValue() + "").equals("")) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";

            String value = wrapper.getValue() + "";
            if (value.length() < minLength) return "TOO_SHORT";
            if (value.length() > maxLength) return "TOO_LONG";

            wrapper.getFieldResultArr().add(value);
            return "";
        };
    };


    public static Function<List<Object>, Function> like = objects -> {
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

        return (Function<FunctionKeeper, Object>) (FunctionKeeper wrapper) -> {
            if (wrapper.getValue() == null || (wrapper.getValue() + "").equals("")) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";

            String value = wrapper.getValue() + "";
            String caseInValue = isIgnoreCase ? value.toLowerCase() : value;
            if (!caseInValue.matches(pattern)) return "WRONG_FORMAT";
            wrapper.getFieldResultArr().add(value);
            return "";
        };
    };

}



