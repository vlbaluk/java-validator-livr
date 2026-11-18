package livr

import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

/**
 * Created by vladislavbaluk on 10/19/2017.
 */
class AutoTrimTest extends Specification {
    def "NEGATIVE: Validate data with automatic trim"() {
        when:
        def validator = LIVR.validator().init("{" +
                "                           \"code\":     \"required\"," +
                "                          \"password\": [\"required\", { \"min_length\": 3 }]," +
                "                           \"address\":  { \"nested_object\": {" +
                "                               \"street\": { \"min_length\": 5 }" +
                "                           }}" +
                "                       }", true);

        def output = validator.validate("{" +
                "                        \"code\": \"  \"," +
                "                        \"password\": \" 12  \"," +
                "                       \"address\": {" +
                "                               \"street\": \"  hell \"" +
                "                      }" +
                "                       }");
        then:

        assert output == null;
        JSONAssert.assertEquals(validator.getErrors().toJSONString(), "{" +
                "  \"password\": {" +
                "    \"code\": \"TOO_SHORT\"," +
                "    \"fieldValue\": \"12\"" +
                "  }," +
                "  \"code\": {" +
                "    \"code\": \"REQUIRED\"," +
                "    \"fieldValue\": \"\"" +
                "  }," +
                "  \"address\": {" +
                "    \"street\": {" +
                "      \"code\": \"TOO_SHORT\"," +
                "      \"fieldValue\": \"hell\"" +
                "    }" +
                "  }" +
                "}", false);

    }

    def "POSITIVE: Validate data with automatic trim"() {
        when:
        def validator = LIVR.validator().init("{" +
                "                           \"code\":     \"required\"," +
                "                          \"password\": [\"required\", { \"min_length\": 3 }]," +
                "                           \"list\":     { \"list_of\": [\"string\"] }," +
                "                           \"address\":  { \"nested_object\": {" +
                "                               \"street\": { \"min_length\": 5 }" +
                "                           }}" +
                "                       }", true);

        def output = validator.validate("{" +
                "                        \"code\": \" A \"," +
                "                        \"password\": \" 123  \"," +
                "                        \"list\": [\" aaa \", \" bbb \"]," +
                "                       \"address\": {" +
                "                               \"street\": \"  hello \"" +
                "                      }" +
                "                       }");
        then:

        assert validator.getErrors() == null;
        JSONAssert.assertEquals("{" +
                "  \"code\": \"A\"," +
                "  \"password\": \"123\"," +
                "  \"list\": [\"aaa\", \"bbb\"]," +
                "  \"address\": {" +
                "    \"street\": \"hello\"" +
                "  }" +
                "}", output.toJSONString(), true);
    }

    def "Automatic trim keeps numbers and nulls"() {
        when:
        def validator = LIVR.validator().init("{" +
                "    \"number\": \"required\"," +
                "    \"empty\":  \"integer\"," +
                "    \"list\":   { \"list_of\": \"integer\" }" +
                "}", true);

        def output = validator.validate("{\"number\": 5, \"empty\": null, \"list\": [1, 2]}");
        then:

        assert validator.getErrors() == null;
        JSONAssert.assertEquals("{\"number\": 5, \"empty\": null, \"list\": [1, 2]}", output.toJSONString(), true);
    }

}
