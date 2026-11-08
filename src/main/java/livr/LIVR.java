package livr;

import livr.Rules.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by vladislavbaluk on 9/29/2017.
 */
public class LIVR {
    static Map<String, Function> rules = new HashMap<>();

    static {
        rules.put("required", CommonRules.required);
        rules.put("any_object", CommonRules.any_object);
        rules.put("not_empty", CommonRules.not_empty);
        rules.put("not_empty_list", CommonRules.not_empty_list);

        rules.put("string", StringRules.string);
        rules.put("eq", StringRules.eq);
        rules.put("one_of", StringRules.one_of);
        rules.put("max_length", StringRules.max_length);
        rules.put("min_length", StringRules.min_length);
        rules.put("length_equal", StringRules.length_equal);
        rules.put("length_between", StringRules.length_between);
        rules.put("like", StringRules.like);

        rules.put("integer", Numeric.integer);
        rules.put("positive_integer", Numeric.positive_integer);
        rules.put("decimal", Numeric.decimal);
        rules.put("positive_decimal", Numeric.positive_decimal);
        rules.put("max_number", Numeric.max_number);
        rules.put("min_number", Numeric.min_number);
        rules.put("number_between", Numeric.number_between);

        rules.put("email", Special.email);
        rules.put("equal_to_field", Special.equal_to_field);
        rules.put("iso_date", Special.iso_date);
        rules.put("url", Special.url);

        rules.put("nested_object", Meta.nested_object);
        rules.put("list_of", Meta.list_of);
        rules.put("list_of_objects", Meta.list_of_objects);
        rules.put("list_of_different_objects", Meta.list_of_different_objects);
        rules.put("variable_object", Meta.variable_object);
        rules.put("or", Meta.or);

        rules.put("default", Modifiers.default1);
        rules.put("to_lc", Modifiers.to_lc);
        rules.put("to_uc", Modifiers.to_uc);
        rules.put("trim", Modifiers.trim);
        rules.put("remove", Modifiers.remove);
        rules.put("leave_only", Modifiers.leave_only);

        // LIVR 2.5
        rules.put("boolean", CommonRules.boolean1);
        rules.put("not_one_of", StringRules.not_one_of);
        rules.put("max_decimals", Numeric.max_decimals);

        rules.put("digits", StringRules.digits);
        rules.put("starts_with", StringRules.starts_with);
        rules.put("ends_with", StringRules.ends_with);
        rules.put("contains", StringRules.contains);

        rules.put("iso_datetime", Dates.iso_datetime);
        rules.put("iso_time", Dates.iso_time);

        rules.put("required_if", CommonRules.required_if);

        rules.put("when", Meta.when);
        rules.put("values", Meta.values);
        rules.put("keys", Meta.keys);
        rules.put("any", Meta.any);

        rules.put("list_length", ListRules.list_length);
        rules.put("unique", ListRules.unique);

        rules.put("split", Modifiers.split);

        rules.put("uuid", Formats.uuid);
        rules.put("ip", Formats.ip);
        rules.put("cidr", Formats.cidr);
        rules.put("hostname", Formats.hostname);
        rules.put("base64", Formats.base64);
        rules.put("hex", Formats.hex);
        rules.put("digest", Formats.digest);
        rules.put("mongo_id", Formats.mongo_id);
        rules.put("hex_color", Formats.hex_color);
        rules.put("mac", Formats.mac);
        rules.put("e164", Formats.e164);
        rules.put("semver", Formats.semver);
        rules.put("jwt", Formats.jwt);

        rules.put("luhn", Checksums.luhn);
        rules.put("credit_card", Checksums.credit_card);
        rules.put("iban", Checksums.iban);
    }

    public static void registerDefaultRules(Map<String, Function> rules) {
        LIVR.rules.putAll(rules);
    }

    public static Validator validator() {
        Validator val = new Validator();
        val.registerDefaultRules(rules);
        return val;
    }
}
