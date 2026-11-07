package livr

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification
import spock.lang.Unroll

/**
 * The LIVR 2.5 test suite (src/test/resources/test_suite_2.5). options.json gives the
 * current time for "today", "yesterday" and "tomorrow": the validator gets a fixed clock.
 */
class TestSuite25Test extends Specification {

    static List<JSONObject> cases(String suite) {
        IterationMethods.listFilesForFolder("test_suite_2.5/" + suite).sort { it.name }
    }

    static Validator validator(JSONObject json) {
        def parser = new JSONParser()
        def validator = LIVR.validator()
        if (json.options) {
            validator.setClock(IterationMethods.fixedClock((String) parser.parse(json.options).now))
        }
        validator.init((JSONObject) parser.parse(json.rules), false)
        if (json.aliases) {
            for (alias in parser.parse(json.aliases)) validator.registerAliasedRule((JSONObject) alias)
        }
        validator
    }

    @Unroll
    def "#suite/#json.name gives the output"() {
        when:
        def validator = validator(json)
        def result = validator.validate(new JSONParser().parse(json.input))

        then:
        validator.getErrors() == null
        JSONAssert.assertEquals(json.output, result.toJSONString(), true)

        where:
        [suite, json] << ["positive", "aliases_positive"].collectMany { suite -> cases(suite).collect { [suite, it] } }
    }

    @Unroll
    def "#suite/#json.name gives the errors"() {
        when:
        def validator = validator(json)
        def result = validator.validate(new JSONParser().parse(json.input))

        then:
        result == null
        JSONAssert.assertEquals(json.errors, JSONObject.toJSONString(validator.getErrors()), true)

        where:
        [suite, json] << ["negative", "aliases_negative"].collectMany { suite -> cases(suite).collect { [suite, it] } }
    }

    @Unroll
    def "schema_errors/#json.name does not build"() {
        when:
        validator(json).prepare()

        then:
        thrown(Exception)

        where:
        json << cases("schema_errors")
    }
}
