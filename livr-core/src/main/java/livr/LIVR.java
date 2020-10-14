package livr;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import livr.rules.CommonRules;
import livr.rules.MetaRules;
import livr.rules.Modifiers;
import livr.rules.NumericRules;
import livr.rules.SpecialRules;
import livr.rules.StringRules;

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

		rules.put("integer", NumericRules.integer);
		rules.put("positive_integer", NumericRules.positive_integer);
		rules.put("decimal", NumericRules.decimal);
		rules.put("positive_decimal", NumericRules.positive_decimal);
		rules.put("max_number", NumericRules.max_number);
		rules.put("min_number", NumericRules.min_number);
		rules.put("number_between", NumericRules.number_between);

		rules.put("email", SpecialRules.email);
		rules.put("equal_to_field", SpecialRules.equal_to_field);
		rules.put("iso_date", SpecialRules.iso_date);
		rules.put("url", SpecialRules.url);

		rules.put("nested_object", MetaRules.nested_object);
		rules.put("list_of", MetaRules.list_of);
		rules.put("list_of_objects", MetaRules.list_of_objects);
		rules.put("list_of_different_objects", MetaRules.list_of_different_objects);
		rules.put("variable_object", MetaRules.variable_object);
		rules.put("or", MetaRules.or);

		rules.put("default", Modifiers.default1);
		rules.put("to_lc", Modifiers.to_lc);
		rules.put("to_uc", Modifiers.to_uc);
		rules.put("trim", Modifiers.trim);
		rules.put("remove", Modifiers.remove);
		rules.put("leave_only", Modifiers.leave_only);
	}

	public static void registerDefaultRules(Map<String, Function> rules) {
		rules.putAll(rules);
	}

	public static Validator validator() {
		Validator val = new Validator();
		val.registerDefaultRules(rules);
		return val;
	}
}
