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
}
