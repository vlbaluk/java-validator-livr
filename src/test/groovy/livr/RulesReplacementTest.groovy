package livr

import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

/**
 * Created by vladislavbaluk on 10/19/2017.
 */
class RulesReplacementTest extends Specification {

    def "Validate data with registered rules"() {
        when:
        Validator validator = LIVR.validator();
        def defaultRules = validator.getDefaultRules();
        def originalRules = new HashMap<>()
        def newRules = new HashMap<>();

        for (String key : defaultRules.keySet()) {
            def ruleBuilder = defaultRules.get(key);
            originalRules.put(key, ruleBuilder);
            newRules.put(key, MyFuncClass.patchRule(key, ruleBuilder));
        }

        validator.registerDefaultRules(newRules);

        validator = validator.init("{" +
                "            \"name\":  [\"required\"]," +
                "            \"phone\": { \"max_length\": 10 }" +
                "}", true);
        def output = validator.validate("{" +
                "\"phone\": \"123456789123456\"" +
                "}");

        then:
        assert output == null;
        JSONAssert.assertEquals(validator.getErrors().toJSONString(), "    {\n" +
                "            \"name\": {\n" +
                "                \"code\": \"REQUIRED\",\n" +
                "                \"rule\": { \"required\": [] }\n" +
                "            },\n" +
                "\n" +
                "            \"phone\": {\n" +
                "                \"code\": \"TOO_LONG\",\n" +
                "                \"rule\": { \"max_length\": [10] }\n" +
                "            }\n" +
                "        }", false);
    }
}
