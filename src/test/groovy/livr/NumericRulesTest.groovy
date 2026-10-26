package livr

import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification
import spock.lang.Unroll

class NumericRulesTest extends Specification {

    @Unroll
    def "#rules accepts #input"() {
        when:
        def validator = LIVR.validator().init(rules, false);
        def output = validator.validate(input);

        then:
        assert validator.getErrors() == null;
        JSONAssert.assertEquals(expected, output.toJSONString(), true);

        where:
        rules                                         | input                    | expected
        '{"n": "integer"}'                            | '{"n": "-12"}'           | '{"n": -12}'
        '{"n": "integer"}'                            | '{"n": "-0"}'            | '{"n": 0}'
        '{"n": "positive_integer"}'                   | '{"n": "42"}'            | '{"n": 42}'
        '{"n": {"min_number": -5}}'                   | '{"n": "-3"}'            | '{"n": -3}'
        '{"n": {"min_number": 0.5}}'                  | '{"n": 0.5}'             | '{"n": 0.5}'
        '{"n": {"max_number": 1.5}}'                  | '{"n": "1.2"}'           | '{"n": 1.2}'
        '{"n": {"number_between": [0.5, 1.5]}}'       | '{"n": 1}'               | '{"n": 1}'
    }

    @Unroll
    def "#rules rejects #input"() {
        when:
        def validator = LIVR.validator().init(rules, false);
        def output = validator.validate(input);

        then:
        assert output == null;
        assert validator.getErrors().get("n").get("code") == code;

        where:
        rules                                         | input                                       | code
        '{"n": "integer"}'                            | '{"n": "--1"}'                              | "NOT_INTEGER"
        '{"n": "integer"}'                            | '{"n": "-"}'                                | "NOT_INTEGER"
        '{"n": "positive_integer"}'                   | '{"n": "-3"}'                               | "NOT_POSITIVE_INTEGER"
        '{"n": "positive_integer"}'                   | '{"n": "0"}'                                | "NOT_POSITIVE_INTEGER"
        '{"n": "positive_integer"}'                   | '{"n": "-123456789012345678901234567890"}'  | "NOT_POSITIVE_INTEGER"
        '{"n": {"min_number": 0.5}}'                  | '{"n": 0.4}'                                | "TOO_LOW"
        '{"n": {"max_number": 1.5}}'                  | '{"n": "1.6"}'                              | "TOO_HIGH"
        '{"n": {"number_between": [0.5, 1.5]}}'       | '{"n": "1.6"}'                              | "TOO_HIGH"
        '{"n": {"number_between": [0.5, 1.5]}}'       | '{"n": 0.4}'                                | "TOO_LOW"
    }
}
