/**
 * Created by vladislavbaluk on 10/3/2017.
 */
package livr

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

class BaseFuncTest extends Specification {
    def "positive"() {
        when:
        def jsons = IterationMethods.listFilesForFolder("positive");
        for (def json : jsons) {
            def parser = new JSONParser();
            def rules = parser.parse(json.rules)
            def input = parser.parse(json.input);
            json.output = parser.parse(json.output);
            def validator = LIVR.validator().init(rules, false);
            def result = validator.validate(input);

            json.result = result;
            json.errorresult = (JSONObject) new JSONParser().parse(JSONObject.toJSONString(validator.getErrors()));
        }

        then:
        jsons.each {
            assert it.errorresult == null ? true : (it.errorresult.keySet.size == 0);
            def res = it.result.toJSONString();
            def out = it.output.toJSONString();

            JSONAssert.assertEquals(res, out, false);
        }
    }

    def "negative"() {

        when:
        def jsons = IterationMethods.listFilesForFolder("negative");
        for (def json : jsons) {
            def parser = new JSONParser();
            def rules = parser.parse(json.rules)
            def input = parser.parse(json.input);
            json.errors = parser.parse(json.errors);
            def validator = LIVR.validator().init(rules, false);
            def result = validator.validate(input);

            json.result = result;
            json.errorresult = (JSONObject) new JSONParser().parse(JSONObject.toJSONString(validator.getErrors()));
        }
        then:
        jsons.each {
            def res = it.errorresult.toJSONString();
            def out = it.errors.toJSONString();
            JSONAssert.assertEquals(res, out, false);
        }

    }

    def "aliases_positive"() {
        when:
        def jsons = IterationMethods.listFilesForFolder("aliases_positive");
        for (def json : jsons) {
            def parser = new JSONParser();
            def rules = parser.parse(json.rules)
            def input = parser.parse(json.input);
            json.output = parser.parse(json.output);
            json.aliases = parser.parse(json.aliases);
            def validator = LIVR.validator().init(rules, false);

            for (alias in json.aliases) {
                validator.registerAliasedRule(alias);
            }
            def result = validator.validate(input);

            json.result = result;
            json.errorresult = (JSONObject) new JSONParser().parse(JSONObject.toJSONString(validator.getErrors()));
        }
        then:
        jsons.each {
            assert it.errorresult == null ? true : (it.errorresult.keySet.size == 0);
            def res = it.result.toJSONString();
            def out = it.output.toJSONString();

            JSONAssert.assertEquals(res, out, false);
        }
    }

    def "aliases_negative"() {
        when:
        def jsons = IterationMethods.listFilesForFolder("aliases_negative");
        for (def json : jsons) {
            def parser = new JSONParser();
            def rules = parser.parse(json.rules)
            def input = parser.parse(json.input);
            json.errors = parser.parse(json.errors);
            json.aliases = parser.parse(json.aliases);
            def validator = LIVR.validator().init(rules, false);

            for (alias in json.aliases) {
                validator.registerAliasedRule(alias);
            }
            def result = validator.validate(input);

            json.result = result;
            json.errorresult = (JSONObject) new JSONParser().parse(JSONObject.toJSONString(validator.getErrors()));
        }
        then:
        jsons.each {
            def res = it.errorresult.toJSONString();
            def out = it.errors.toJSONString();
            JSONAssert.assertEquals(res, out, false);
        }
    }
}