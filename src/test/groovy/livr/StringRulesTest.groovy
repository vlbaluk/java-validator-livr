package livr

import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification
import spock.lang.Unroll

class StringRulesTest extends Specification {

    @Unroll
    def "#rules keeps a list unchanged"() {
        when:
        def validator = LIVR.validator().init(rules, false);
        def output = validator.validate('{"list": [" Ab1 "]}');

        then:
        assert validator.getErrors() == null;
        JSONAssert.assertEquals('{"list": [" Ab1 "]}', output.toJSONString(), true);

        where:
        rules << [
                '{"list": "trim"}',
                '{"list": "to_lc"}',
                '{"list": "to_uc"}',
                '{"list": {"remove": "b"}}',
                '{"list": {"leave_only": "1"}}'
        ]
    }

    @Unroll
    def "like with the i flag matches #value against #pattern"() {
        when:
        def validator = LIVR.validator().init('{"word": {"like": ["' + pattern + '", "i"]}}', false);
        def output = validator.validate('{"word": "' + value + '"}');

        then:
        assert validator.getErrors() == null;
        assert output.get("word") == value;

        where:
        pattern      | value
        '^abc$'      | 'ABC'
        '^ABC$'      | 'abc'
        '^[A-Z]+$'   | 'abC'
        '^\u0457\u0436$' | '\u0407\u0416'
        '^\u0407\u0416$' | '\u0457\u0436'
    }

    def "like without flags is case sensitive"() {
        when:
        def validator = LIVR.validator().init('{"word": {"like": "^abc$"}}', false);
        def output = validator.validate('{"word": "ABC"}');

        then:
        assert output == null;
        assert validator.getErrors().get("word").get("code") == "WRONG_FORMAT";
    }

    @Unroll
    def "#rules matches the boolean #value as \"true\" or \"false\", in both directions (LIVR 2.5, 2.3)"() {
        when:
        def validator = LIVR.validator().init(rules, false);
        def output = validator.validate('{"flag": ' + value + '}');

        then:
        assert validator.getErrors() == null;
        JSONAssert.assertEquals('{"flag": ' + expected + '}', output.toJSONString(), true);

        where:
        rules                                    | value   | expected
        '{"flag": {"eq": "true"}}'               | 'true'  | '"true"'
        '{"flag": {"eq": "false"}}'              | 'false' | '"false"'
        '{"flag": {"one_of": ["true", "false"]}}' | 'true'  | '"true"'
        '{"flag": {"one_of": ["true", "false"]}}' | 'false' | '"false"'
        '{"flag": {"eq": true}}'                 | '"true"' | 'true'
        '{"flag": {"eq": false}}'                | '"false"' | 'false'
        '{"flag": {"one_of": [true, false]}}'    | '"true"' | 'true'
        '{"flag": {"one_of": [true, false]}}'    | '"false"' | 'false'
        '{"flag": {"eq": true}}'                 | 'true'  | 'true'
    }

    @Unroll
    def "#rules does not match #value, as in LIVR 2.0"() {
        when:
        def validator = LIVR.validator().init(rules, false);
        def output = validator.validate('{"flag": ' + value + '}');

        then:
        assert output == null;
        assert validator.getErrors().get("flag").get("code") == "NOT_ALLOWED_VALUE";

        where:
        rules                                    | value
        '{"flag": {"eq": "True"}}'               | 'true'
        '{"flag": {"eq": "1"}}'                  | 'true'
        '{"flag": {"one_of": ["1", "0"]}}'       | 'false'
        '{"flag": {"eq": true}}'                 | '"True"'
        '{"flag": {"eq": true}}'                 | '"1"'
        '{"flag": {"one_of": [true]}}'           | '"TRUE"'
    }
}
