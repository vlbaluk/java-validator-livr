package livr

import org.json.simple.JSONObject
import spock.lang.Specification
import spock.lang.Unroll

class Livr25RulesTest extends Specification {

    @Unroll
    def "when checks a referenced #type with #condition"() {
        when:
        def validator = LIVR.validator().init('{"x": {"when": ["s", "' + condition + '", "required"]}, "s": "any"}', false)

        then:
        validator.validate(input) == null
        validator.getErrors().get("x").get("code") == "REQUIRED"

        where:
        type     | condition        | input
        "object" | "any_object"     | '{"s": {}}'
        "list"   | "not_empty_list" | '{"s": [1]}'
    }

    def "semver checks a long version without a StackOverflowError"() {
        when:
        def version = '1.0.0-' + 'a.' * 10000 + 'a+' + 'b.' * 10000 + 'b'
        def output = LIVR.validator().init('{"v": "semver"}', false).validate(new JSONObject([v: version]))

        then:
        output.get("v") == version
    }
}
