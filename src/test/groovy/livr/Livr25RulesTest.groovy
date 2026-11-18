package livr

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
}
