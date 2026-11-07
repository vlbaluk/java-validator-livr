package livr;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;

/**
 * The list that a rule builder gets: the rule arguments and the rule builders, as before, and
 * also the arguments as they are written in the schema and the clock of the validator.
 *
 * The LIVR 2.0 rules get their argument as objects.get(0), where a single list argument is
 * unwrapped ({"one_of": [["a", "b"]]} is the same as {"one_of": ["a", "b"]}). The LIVR 2.5 rules
 * take their arguments in order, like the parameters of a function (LIVR 2.5, 2.6), so they need
 * the arguments as written: {"not_one_of": [["a", "b"]]} is one list argument, a schema error.
 */
public class RuleArguments extends ArrayList<Object> {
    private static final Pattern DECIMAL = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");

    private final List<Object> arguments;
    private final Clock clock;

    public RuleArguments(List<Object> objects, List<Object> arguments, Clock clock) {
        super(objects);
        this.arguments = arguments;
        this.clock = clock;
    }

    /**
     * The arguments of a rule as written in the schema: "rule" and {"rule": []} give [],
     * {"rule": x} gives [x], {"rule": [x, y]} gives [x, y]. {"rule": null} is one null argument,
     * not "no arguments" (LIVR 2.5, 2.6): no LIVR 2.5 rule takes null, so it is a schema error
     * for them; the LIVR 2.0 rules and custom rules get null as one argument, as in LIVR 2.0
     */
    public static List<Object> get(List<Object> objects) {
        if (objects instanceof RuleArguments) return ((RuleArguments) objects).arguments;

        // A rule builder called directly, without a validator: the arguments are the first object
        if (objects.size() < 2) return Collections.emptyList();
        Object args = objects.get(0);
        if (args instanceof JSONArray) return (JSONArray) args;
        return Collections.singletonList(args);
    }

    /**
     * The clock of the validator that builds the rule, for "today", "yesterday" and "tomorrow"
     */
    public static Clock clock(List<Object> objects) {
        if (objects instanceof RuleArguments) return ((RuleArguments) objects).clock;
        return Clock.systemUTC();
    }

    /**
     * The rule builders, the last object
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Function> ruleBuilders(List<Object> objects) {
        return (Map<String, Function>) objects.get(objects.size() - 1);
    }

    /**
     * A schema error for a rule without arguments that gets them (LIVR 2.5, 2.6)
     */
    public static void none(List<Object> objects, String ruleName) {
        if (!get(objects).isEmpty()) throw schemaError("\"" + ruleName + "\" takes no arguments");
    }

    /**
     * The arguments of a rule that takes one or more strings; a number is its string form
     */
    public static List<String> strings(List<Object> objects, String ruleName) {
        List<Object> args = get(objects);
        if (args.isEmpty()) throw schemaError("\"" + ruleName + "\" needs an argument");

        List<String> strings = new ArrayList<>();
        for (Object arg : args) strings.add(string(arg, ruleName));
        return strings;
    }

    /**
     * A string argument; a number is its string form (LIVR 2.5, 2.6)
     */
    public static String string(Object arg, String ruleName) {
        if (arg instanceof String) return (String) arg;
        if (arg instanceof Number) return LIVRUtils.stringForm(arg);
        throw schemaError("an argument of \"" + ruleName + "\" must be a string");
    }

    /**
     * A number argument: a number, or a string in the grammar of "decimal" (LIVR 2.5, 2.4 and 2.6):
     * "2" and "2.0" are 2, " 2", "1e1" and "0x40" are not numbers. Null for anything else
     */
    public static BigDecimal number(Object arg) {
        if (arg instanceof Number) return new BigDecimal(LIVRUtils.stringForm(arg));
        if (arg instanceof String && DECIMAL.matcher((String) arg).matches()) return new BigDecimal((String) arg);
        return null;
    }

    /**
     * A non-negative integer argument (LIVR 2.5, 2.6)
     */
    public static int nonNegativeInteger(Object arg, String ruleName) {
        BigDecimal number = number(arg);
        boolean isValid = number != null && number.signum() >= 0 && number.stripTrailingZeros().scale() <= 0
                && number.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0;
        if (!isValid) throw schemaError("an argument of \"" + ruleName + "\" must be a non-negative integer");
        return number.intValueExact();
    }

    public static IllegalArgumentException schemaError(String message) {
        return new IllegalArgumentException("LIVR: " + message);
    }
}
