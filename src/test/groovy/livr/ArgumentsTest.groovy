package livr

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Arguments of the rules: extra arguments of the LIVR 2.0 rules are ignored and custom rules keep
 * working in the metarules; the LIVR 2.5 rules check their arguments (LIVR 2.5, 2.6)
 */
class ArgumentsTest extends Specification {

    static Validator validator(String rules) {
        def validator = LIVR.validator().init(rules, false)
        validator.registerRules(["my_trim": MyFuncClass.my_trim])
        validator
    }

    @Unroll
    def "LIVR 2.0 #rule ignores an extra argument and keeps custom rules"() {
        when:
        def validator = validator(rules)
        def result = validator.validate(input)

        then:
        validator.getErrors() == null
        result == new JSONParser().parse(output)

        where:
        rule                        | rules                                                                                                  | input                                                   | output
        "nested_object"             | '{"a": {"nested_object": [{"b": "my_trim"}, "extra"]}}'                                                | '{"a": {"b": " x "}}'                                   | '{"a": {"b": "x"}}'
        "list_of"                   | '{"a": {"list_of": [["my_trim"], "extra"]}}'                                                           | '{"a": [" x "]}'                                        | '{"a": ["x"]}'
        "list_of_objects"           | '{"a": {"list_of_objects": [{"b": "my_trim"}, "extra"]}}'                                              | '{"a": [{"b": " x "}]}'                                 | '{"a": [{"b": "x"}]}'
        "variable_object"           | '{"a": {"variable_object": ["type", {"t": {"type": "required", "b": "my_trim"}}, "extra"]}}'           | '{"a": {"type": "t", "b": " x "}}'                      | '{"a": {"type": "t", "b": "x"}}'
        "list_of_different_objects" | '{"a": {"list_of_different_objects": ["type", {"t": {"type": "required", "b": "my_trim"}}, "extra"]}}' | '{"a": [{"type": "t", "b": " x "}]}'                    | '{"a": [{"type": "t", "b": "x"}]}'
        "like"                      | '{"a": {"like": ["^[a-z]+$", "i", "extra"]}}'                                                          | '{"a": "ABC"}'                                          | '{"a": "ABC"}'
    }

    @Unroll
    def "LIVR 2.5 rule #rules builds"() {
        when:
        validator(rules).prepare()

        then:
        notThrown(Exception)

        where:
        rules << [
                '{"a": {"max_decimals": "2.0"}}',
                '{"a": {"max_decimals": "02"}}',
                '{"a": {"mac": "64"}}',
                '{"a": {"mac": "64.0"}}',
                '{"a": {"list_length": "3"}}',
                '{"a": {"list_length": [{"min": 1}, "x"]}}',
                '{"a": {"split": [",", {}]}}',
                '{"a": {"iso_date": [{"min": "2026-01-01"}, "x"]}}',
                '{"a": {"iso_date": {"min": "2026-12-01", "max": "today"}}}',
                '{"a": {"when": ["b", "required", "my_trim", "any", {}]}}',
                '{"a": {"values": "my_trim"}}',
                '{"a": {"iso_date": null}}',
                '{"a": {"iso_date": [null]}}',
        ]
    }

    @Unroll
    def "LIVR 2.5 rule #rules is a schema error"() {
        when:
        validator(rules).prepare()

        then:
        thrown(IllegalArgumentException)

        where:
        rules << [
                '{"a": {"max_decimals": " 2"}}',
                '{"a": {"max_decimals": "1e1"}}',
                '{"a": {"max_decimals": "0x40"}}',
                '{"a": {"max_decimals": "2.5"}}',
                '{"a": {"mac": "0x40"}}',
                '{"a": {"list_length": [3, "x"]}}',
                '{"a": {"list_length": [3, {"max": 4}]}}',
                '{"a": {"when": [1, "required", "string"]}}',
                '{"a": {"when": [1, {"x": "string"}]}}',
                '{"a": {"required_if": {"": "x"}}}',
                '{"a": {"when": ["type", {"card": null}]}}',
                '{"a": {"when": ["type", "required", ["required", []]]}}',
                '{"a": {"values": ["required", []]}}',
                '{"a": {"list_length": {"min": null}}}',
                '{"a": {"list_length": {"min": 1, "max": null}}}',
                '{"a": {"list_length": null}}',
                '{"a": {"split": null}}',
                '{"a": {"not_one_of": [null, "x"]}}',
                '{"a": {"required_if": {"type": null}}}',
                '{"a": {"iso_date": [null, null]}}',
                '{"a": {"boolean": null}}',
                '{"a": {"boolean": [null]}}',
                '{"a": {"unique": null}}',
                '{"a": {"uuid": null}}',
                '{"a": {"any": null}}',
                '{"a": {"base64": null}}',
                '{"a": {"mac": null}}',
                '{"a": {"ip": null}}',
                '{"a": {"max_decimals": null}}',
                '{"a": {"not_one_of": null}}',
                '{"a": {"required_if": null}}',
                '{"a": {"when": null}}',
                '{"a": {"values": null}}',
                '{"a": {"keys": null}}',
                '{"a": {"iso_datetime": null}}',
                '{"a": {"iso_time": null}}',
        ]
    }

    def "the result of a custom rule in a LIVR 2.5 metarule"() {
        when:
        def validator = validator('{"a": {"when": ["b", {"eq": "x"}, "my_trim"]}, "b": "string"}')

        then:
        validator.validate('{"a": " y ", "b": "x"}') == (JSONObject) new JSONParser().parse('{"a": "y", "b": "x"}')
    }

    @Unroll
    def "#rules is #plain: LIVR 2.0 ignored the arguments of iso_date (LIVR 2.5, 2.6)"() {
        when:
        def nullArgument = validator(rules)
        def plainRule = validator(plain)
        def result = nullArgument.validate(input)

        then:
        result == plainRule.validate(input)
        nullArgument.getErrors() == plainRule.getErrors()

        where:
        rules                         | plain                      | input
        '{"a": {"iso_date": null}}'   | '{"a": "iso_date"}'        | '{"a": "2026-01-31"}'
        '{"a": {"iso_date": null}}'   | '{"a": "iso_date"}'        | '{"a": "2026-02-30"}'
        '{"a": {"iso_date": null}}'   | '{"a": "iso_date"}'        | '{"a": "1582-10-10"}'
        '{"a": {"iso_date": [null]}}' | '{"a": "iso_date"}'        | '{"a": "1500-02-29"}'
    }

    def "LIVR 2.0 default gets null as its argument and sets null"() {
        when:
        def validator = validator('{"a": {"default": null}, "b": [{"default": null}, "string"]}')

        then:
        validator.validate('{}') == (JSONObject) new JSONParser().parse('{"a": null, "b": null}')
    }

    def "a custom rule gets null as one argument"() {
        given:
        def received = []
        def validator = LIVR.validator().init('{"a": {"my_rule": null}}', false)
        validator.registerRules(["my_rule": { List<Object> objects ->
            received << objects.get(0) << RuleArguments.get(objects)
            return { FunctionKeeper wrapper -> "" } as java.util.function.Function
        } as java.util.function.Function])

        when:
        validator.prepare()

        then:
        received == [null, [null]]
    }

    @Unroll
    def "unique with keys gives FORMAT_ERROR for #item, another key is empty"() {
        when:
        def validator = validator('{"a": {"unique": ["x", "y"]}}')

        then:
        validator.validate('{"a": [' + item + ']}') == null
        validator.getErrors().get("a").get("code") == "FORMAT_ERROR"

        where:
        item << [
                '{"x": "", "y": {"b": 1}}',
                '{"x": [1], "y": ""}',
                '{"y": [1]}',
                '{"x": null, "y": {}}',
        ]
    }
}
