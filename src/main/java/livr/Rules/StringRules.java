package livr.Rules;

import com.google.common.collect.Lists;
import livr.FunctionKeeper;
import livr.LIVRUtils;
import livr.RuleArguments;
import org.json.simple.JSONArray;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Pattern;
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
        final List<Object> allowedValues = objects.get(0) instanceof JSONArray ? (JSONArray) objects.get(0) : Lists.newArrayList(objects.get(0));

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

        final Pattern regex = isIgnoreCase
                ? Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                : Pattern.compile(pattern);

        return (Function<FunctionKeeper, Object>) (FunctionKeeper wrapper) -> {
            if (wrapper.getValue() == null || (wrapper.getValue() + "").equals("")) return "";
            if (!LIVRUtils.isPrimitiveValue(wrapper.getValue())) return "FORMAT_ERROR";

            String value = wrapper.getValue() + "";
            if (!regex.matcher(value).matches()) return "WRONG_FORMAT";
            wrapper.getFieldResultArr().add(value);
            return "";
        };
    };

    /**
     * The value must not equal any of the arguments, compared as strings (LIVR 2.5, 2.3)
     */
    public static Function<List<Object>, Function> not_one_of = objects -> {
        List<Object> args = RuleArguments.get(objects);
        if (args.isEmpty()) throw RuleArguments.schemaError("\"not_one_of\" needs one or more values");

        Set<String> forbiddenValues = new HashSet<>();
        for (Object arg : args) {
            if (!LIVRUtils.isPrimitive(arg)) {
                throw RuleArguments.schemaError("the values of \"not_one_of\" must be strings, numbers or booleans");
            }
            forbiddenValues.add(LIVRUtils.stringForm(arg));
        }

        return (Function<FunctionKeeper, Object>) (FunctionKeeper wrapper) -> {
            if (LIVRUtils.isEmpty(wrapper.getValue())) return "";
            if (!LIVRUtils.isPrimitive(wrapper.getValue())) return "FORMAT_ERROR";

            if (forbiddenValues.contains(LIVRUtils.stringForm(wrapper.getValue()))) return "NOT_ALLOWED_VALUE";
            return "";
        };
    };

    /**
     * One or more ASCII digits; leading zeros are kept and the output is a string (LIVR 2.5)
     */
    public static Function<List<Object>, Function> digits = objects -> {
        RuleArguments.none(objects, "digits");
        Pattern digits = Pattern.compile("[0-9]+");

        return (Function<FunctionKeeper, Object>) (FunctionKeeper wrapper) -> {
            if (LIVRUtils.isEmpty(wrapper.getValue())) return "";
            if (!LIVRUtils.isPrimitive(wrapper.getValue())) return "FORMAT_ERROR";

            String value = LIVRUtils.stringForm(wrapper.getValue());
            if (!digits.matcher(value).matches()) return "NOT_DIGITS";
            wrapper.getFieldResultArr().add(value);
            return "";
        };
    };

    public static Function<List<Object>, Function> starts_with = objects -> affix(objects, "starts_with", String::startsWith);

    public static Function<List<Object>, Function> ends_with = objects -> affix(objects, "ends_with", String::endsWith);

    public static Function<List<Object>, Function> contains = objects -> affix(objects, "contains", String::contains);

    /**
     * starts_with, ends_with and contains: the string form must start with, end with or contain
     * at least one of the arguments, non-empty strings. Exact and case-sensitive (LIVR 2.5)
     */
    private static Function affix(List<Object> objects, String ruleName, BiPredicate<String, String> test) {
        List<String> parts = RuleArguments.strings(objects, ruleName);
        if (parts.contains("")) throw RuleArguments.schemaError("the arguments of \"" + ruleName + "\" must be non-empty strings");

        return (Function<FunctionKeeper, Object>) (FunctionKeeper wrapper) -> {
            if (LIVRUtils.isEmpty(wrapper.getValue())) return "";
            if (!LIVRUtils.isPrimitive(wrapper.getValue())) return "FORMAT_ERROR";

            String value = LIVRUtils.stringForm(wrapper.getValue());
            for (String part : parts) {
                if (test.test(value, part)) {
                    wrapper.getFieldResultArr().add(value);
                    return "";
                }
            }
            return "WRONG_FORMAT";
        };
    }
}
